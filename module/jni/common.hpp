#pragma once

#include <sys/system_properties.h>

#include <list>
#include <shared_mutex>
#include <string_view>

#include "log.h"
#include "lsplant.hpp"
#include "utils/hook_helper.hpp"

namespace lsplant {

enum class Arch {
    kArm,
    kArm64,
};

consteval inline Arch GetArch() {
#if defined(__arm__)
    return Arch::kArm;
#elif defined(__aarch64__)
    return Arch::kArm64;
#else
#error "unsupported architecture"
#endif
}

inline static constexpr auto kArch = GetArch();

template <typename T>
constexpr inline auto RoundUpTo(T v, size_t size) {
    return v + size - 1 - ((v + size - 1) & (size - 1));
}

template<typename T, size_t N>
[[gnu::always_inline]] constexpr inline size_t arraysize(T(&)[N]) {
    return N;
}

inline auto GetAndroidApiLevel() {
    static auto kApiLevel = []() {
        std::array<char, PROP_VALUE_MAX> prop_value;
        __system_property_get("ro.build.version.sdk", prop_value.data());
        int base = atoi(prop_value.data());
        __system_property_get("ro.build.version.preview_sdk", prop_value.data());
        return base + atoi(prop_value.data());
    }();
    return kApiLevel;
}

inline static constexpr auto kPointerSize = sizeof(void *);

}  // namespace lsplant
