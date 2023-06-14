#pragma once

#include <jni.h>

#include <string_view>

/// \namespace lsplant
namespace lsplant {

inline namespace v2 {
/// \struct InitInfo
/// \brief Information and configuration that are needed to call #Init()
struct InitInfo {
    /// \brief Type of inline hook function.
    /// In \ref std::function form so that user can use lambda expression with capture list.<br>
    /// \p target is the target function to be hooked.<br>
    /// \p hooker is the hooker function to replace the \p target function.<br>
    /// \p return is the backup function that points to the previous target function.
    /// it should return null if hook fails and nonnull if successes.
    using InlineHookFunType = std::function<void *(void *target, void *hooker)>;
    /// \brief Type of inline unhook function.
    /// In \ref std::function form so that user can use lambda expression with capture list.<br>
    /// \p func is the target function that is previously hooked.<br>
    /// \p return should indicate the status of unhooking.<br>
    using InlineUnhookFunType = std::function<bool(void *func)>;
    /// \brief Type of symbol resolver to \p libart.so.
    /// In \ref std::function form so that user can use lambda expression with capture list.<br>
    /// \p symbol_name is the symbol name that needs to retrieve.<br>
    /// \p return is the absolute address in the memory that points to the target symbol. It should
    /// be null if the symbol cannot be found. <br>
    /// \note It should be able to resolve symbols from both .dynsym and .symtab.
    using ArtSymbolResolver = std::function<void *(std::string_view symbol_name)>;

    /// \brief Type of prefix symbol resolver to \p libart.so.
    /// In \ref std::function form so that user can use lambda expression with capture list.<br>
    /// \p symbol_prefix is the symbol prefix that needs to retrieve.<br>
    /// \p return is the first absolute address in the memory that points to the target symbol.
    /// It should be null if the symbol cannot be found. <br>
    /// \note It should be able to resolve symbols from both .dynsym and .symtab.
    using ArtSymbolPrefixResolver = std::function<void *(std::string_view symbol_prefix)>;

    /// \brief The inline hooker function. Must not be null.
    InlineHookFunType inline_hooker;
    /// \brief The inline unhooker function. Must not be null.
    InlineUnhookFunType inline_unhooker;
    /// \brief The symbol resolver to \p libart.so. Must not be null.
    ArtSymbolResolver art_symbol_resolver;

    /// \brief The symbol prefix resolver to \p libart.so. May be null.
    ArtSymbolPrefixResolver art_symbol_prefix_resolver;
};

/// \brief Initialize LSPlant for the proceeding hook.
/// It mainly prefetch needed symbols and hook some functions.
/// The env should not have any restriction for accessing hidden APIs.
/// You can obtain such a \ref JNIEnv in JNI_OnLoad().
/// \param[in] env The Java environment. Must not be null.
/// \param[in] info The information for initialized.
/// Basically, the info provides the inline hooker and unhooker together with a symbol resolver of
/// libart.so to hook and extract needed native functions of ART.
/// \return Indicate whether initialization succeed. Behavior is undefined if calling other
/// LSPlant interfaces before initialization or after a fail initialization.
/// \see InitInfo.
[[nodiscard, maybe_unused, gnu::visibility("default")]] bool Init(JNIEnv *env,
                                                                  const InitInfo &info);


[[maybe_unused, gnu::visibility("default")]] void HookMethod(JNIEnv* env,
                                                             jobject target,
                                                             jobject hook,
                                                             jobject backup);

}  // namespace v1
}  // namespace lsplant
