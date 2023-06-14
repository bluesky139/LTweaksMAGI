#include "lsplant.hpp"

#include <android/api-level.h>
#include <sys/mman.h>
#include <sys/system_properties.h>
#include <unistd.h>
#include <array>
#include <atomic>

#include "art/runtime/art_method.hpp"
#include "common.hpp"
#include "utils/jni_helper.hpp"

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-pragmas"
#pragma ide diagnostic ignored "ConstantConditionsOC"
#pragma ide diagnostic ignored "Simplify"
#pragma ide diagnostic ignored "UnreachableCode"
namespace lsplant {

using art::ArtMethod;

namespace {
template <typename T, T... chars>
inline consteval auto operator""_uarr() {
    return std::array<uint8_t, sizeof...(chars)>{static_cast<uint8_t>(chars)...};
}

consteval inline auto GetTrampoline() {
    if constexpr (kArch == Arch::kArm) {
        return std::make_tuple("\x00\x00\x9f\xe5\x00\xf0\x90\xe5\x78\x56\x34\x12"_uarr,
                               // NOLINTNEXTLINE
                               uint8_t{32u}, uintptr_t{8u});
    }
    if constexpr (kArch == Arch::kArm64) {
        return std::make_tuple(
            "\x60\x00\x00\x58\x10\x00\x40\xf8\x00\x02\x1f\xd6\x78\x56\x34\x12\x78\x56\x34\x12"_uarr,
            // NOLINTNEXTLINE
            uint8_t{44u}, uintptr_t{12u});
    }
}

auto [trampoline, entry_point_offset, art_method_offset] = GetTrampoline();

inline void UpdateTrampoline(uint8_t offset) {
    trampoline[entry_point_offset / CHAR_BIT] |= offset << (entry_point_offset % CHAR_BIT);
    trampoline[entry_point_offset / CHAR_BIT + 1] |=
        offset >> (CHAR_BIT - entry_point_offset % CHAR_BIT);
}

bool InitNative(JNIEnv *env, const HookHandler &handler) {
    if (!handler.inline_hooker || !handler.inline_unhooker || !handler.art_symbol_resolver) {
        return false;
    }
    if (!ArtMethod::Init(env, handler)) {
        LOGE("Failed to init art method");
        return false;
    }
    UpdateTrampoline(ArtMethod::GetEntryPointOffset());
    return true;
}

static_assert(std::endian::native == std::endian::little, "Unsupported architecture");

union Trampoline {
public:
    uintptr_t address;
    unsigned count : 12;
};

static_assert(sizeof(Trampoline) == sizeof(uintptr_t), "Unsupported architecture");
static_assert(std::atomic_uintptr_t::is_always_lock_free, "Unsupported architecture");

std::atomic_uintptr_t trampoline_pool{0};
std::atomic_flag trampoline_lock{false};
constexpr size_t kTrampolineSize = RoundUpTo(sizeof(trampoline), kPointerSize);
constexpr size_t kPageSize = 4096;  // assume
constexpr size_t kTrampolineNumPerPage = kPageSize / kTrampolineSize;
constexpr uintptr_t kAddressMask = 0xFFFU;

void *GenerateTrampolineFor(art::ArtMethod *hook) {
    unsigned count;
    uintptr_t address;
    while (true) {
        auto tl = Trampoline{.address = trampoline_pool.fetch_add(1, std::memory_order_release)};
        count = tl.count;
        address = tl.address & ~kAddressMask;
        if (address == 0 || count >= kTrampolineNumPerPage) {
            if (trampoline_lock.test_and_set(std::memory_order_acq_rel)) {
                trampoline_lock.wait(true, std::memory_order_acquire);
                continue;
            }
            address = reinterpret_cast<uintptr_t>(mmap(nullptr, kPageSize,
                                                       PROT_READ | PROT_WRITE | PROT_EXEC,
                                                       MAP_ANONYMOUS | MAP_PRIVATE, -1, 0));
            if (address == reinterpret_cast<uintptr_t>(MAP_FAILED)) {
                PLOGE("mmap trampoline");
                trampoline_lock.clear(std::memory_order_release);
                trampoline_lock.notify_all();
                return nullptr;
            }
            count = 0;
            tl.address = address;
            tl.count = count + 1;
            trampoline_pool.store(tl.address, std::memory_order_release);
            trampoline_lock.clear(std::memory_order_release);
            trampoline_lock.notify_all();
        }
        LOGV("trampoline: count = %u, address = %zx, target = %zx", count, address,
             address + count * kTrampolineSize);
        address = address + count * kTrampolineSize;
        break;
    }
    auto *address_ptr = reinterpret_cast<char *>(address);
    std::memcpy(address_ptr, trampoline.data(), trampoline.size());

    *reinterpret_cast<art::ArtMethod **>(address_ptr + art_method_offset) = hook;

    __builtin___clear_cache(address_ptr, reinterpret_cast<char *>(address + trampoline.size()));

    return address_ptr;
}

bool DoHook(ArtMethod *target, ArtMethod *hook, ArtMethod *backup) {
    LOGV("Hooking: target = %s(%p), hook = %s(%p), backup = %s(%p)", target->PrettyMethod().c_str(),
         target, hook->PrettyMethod().c_str(), hook, backup->PrettyMethod().c_str(), backup);

    if (auto *entrypoint = GenerateTrampolineFor(hook); !entrypoint) {
        LOGE("Failed to generate trampoline");
        return false;
        // NOLINTNEXTLINE
    } else {
        LOGV("Generated trampoline %p", entrypoint);

        target->SetNonCompilable();
        hook->SetNonCompilable();

        // copy after setNonCompilable
        backup->CopyFrom(target);

        target->ClearFastInterpretFlag();

        target->SetEntryPoint(entrypoint);

        if (!backup->IsStatic()) backup->SetPrivate();

        LOGV("Done hook: target(%p:0x%x) -> %p; backup(%p:0x%x) -> %p; hook(%p:0x%x) -> %p", target,
             target->GetAccessFlags(), target->GetEntryPoint(), backup, backup->GetAccessFlags(),
             backup->GetEntryPoint(), hook, hook->GetAccessFlags(), hook->GetEntryPoint());

        return true;
    }
}
}  // namespace

inline namespace v2 {

[[maybe_unused]] bool Init(JNIEnv *env, const InitInfo &info) {
    bool static kInit = InitNative(env, info);
    return kInit;
}

[[maybe_unused]] void HookMethod(JNIEnv* env, jobject target, jobject hook, jobject backup) {
    auto* art_target = ArtMethod::FromReflectedMethod(env, target);
    auto* art_hook = ArtMethod::FromReflectedMethod(env, hook);
    auto* art_backup = ArtMethod::FromReflectedMethod(env, backup);

    DoHook(art_target, art_hook, art_backup);
    env->NewGlobalRef(hook);
    env->NewGlobalRef(backup);
    //LOGW("our HookMethod end.");
}

}  // namespace v2

}  // namespace lsplant

#pragma clang diagnostic pop
