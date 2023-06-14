#include <cstdlib>
#include <unistd.h>
#include <fcntl.h>
#include <jni.h>
#include <sys/types.h>
#include <malloc.h>
#include <cstring>
#include <string>
#include "log.h"
#include "zygisk.hpp"
#include "lsplant.hpp"
#include "elf_util.h"
#include "common.hpp"
#include "utils/jni_helper.hpp"

using zygisk::Api;
using zygisk::AppSpecializeArgs;
using zygisk::ServerSpecializeArgs;

const char* FOLDER = "/data/local/LTweaksMAGI";
const char* DEX_PATH = "/data/local/LTweaksMAGI/MAGI.dex";
const char* MAIN_CLASS = "li.lingfeng.magi.Loader";
const char* MAIN_METHOD = "load";
const char* kLibArtName = "libart.so";
static bool hookInitialized = false;

std::string jstringToString(JNIEnv* env, jstring& jstr) {
    if (jstr) {
        const char* ch = env->GetStringUTFChars(jstr, 0);
        std::string str = std::string(ch);
        env->ReleaseStringUTFChars(jstr, ch);
        return str;
    }
    return std::string();
}

int FakeHookFunction(void *original, void *replace, void **backup) {
    LOGW("FakeHookFunction %p", original);
    return 0;
}

int FakeUnhookFunction(void *original) {
    LOGW("FakeUnhookFunction %p", original);
    return 0;
}

std::unique_ptr<const SandHook::ElfImg> &GetArt(bool release = false) {
    static std::unique_ptr<const SandHook::ElfImg> kArtImg = nullptr;
    if (release) {
        kArtImg.reset();
    } else if (!kArtImg) {
        kArtImg = std::make_unique<SandHook::ElfImg>(kLibArtName);
    }
    return kArtImg;
}

extern "C"
JNIEXPORT void JNICALL
NativeHookMethod(JNIEnv* env, jclass _, jobject target, jobject hook, jobject backup) {
    if (!hookInitialized) {
        hookInitialized = true;
        lsplant::InitInfo initInfo{
                .inline_hooker = [](auto t, auto r) {
                    void *bk = nullptr;
                    return FakeHookFunction(t, r, &bk) == 0 ? bk : nullptr;
                },
                .inline_unhooker = [](auto t) {
                    return FakeUnhookFunction(t) == 0;
                },
                .art_symbol_resolver = [](auto symbol) {
                    return GetArt()->getSymbAddress(symbol);
                },
                .art_symbol_prefix_resolver = [](auto symbol) {
                    return GetArt()->getSymbPrefixFirstAddress(symbol);
                },
        };
        if (!lsplant::Init(env, initInfo)) {
            LOGE("Failed to init lsplant");
            return;
        }
        LOGI("lsplant init ok.");
        GetArt(true);
    }
    lsplant::HookMethod(env, target, hook, backup);
}

static JNINativeMethod gJni_Methods_table[] = {
        {"nativeHookMethod", "(Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;)V", (void*) NativeHookMethod},
};

class MyModule : public zygisk::ModuleBase {
public:
    void onLoad(Api* api, JNIEnv* env) override {
        this->api = api;
        this->env = env;
    }

    void postAppSpecialize(const AppSpecializeArgs* args) override {
        jboolean hasHook = false;
        std::string niceName = jstringToString(env, args->nice_name);
        if (niceName.size() > 100) {
            LOGW("Nice name too long, %s", niceName.c_str());
        } else {
            char prefPath[150] = {0};
            sprintf(prefPath, "%s/%s.pref", FOLDER, niceName.c_str());
            if (access(prefPath, F_OK) == 0) {
                LOGD("Load dex for %s", niceName.c_str());
                std::string appDataDir = jstringToString(env, args->app_data_dir);
                hasHook = loadDex(env, niceName.c_str(), appDataDir);
            }
        }

        if (!hasHook) {
            // Since we do not hook any functions, we should let Zygisk dlclose ourselves
            api->setOption(zygisk::Option::DLCLOSE_MODULE_LIBRARY);
        }
    }

    void postServerSpecialize(const ServerSpecializeArgs *args) override {
        std::string appDataDir;
        jboolean hasHook = loadDex(env, "android", appDataDir);

        if (!hasHook) {
            // Since we do not hook any functions, we should let Zygisk dlclose ourselves
            //api->setOption(zygisk::Option::DLCLOSE_MODULE_LIBRARY);
        }
    }

private:
    Api* api;
    JNIEnv* env;

    jboolean loadDex(JNIEnv* env, const char* niceName, std::string& appDataDir) {
        if (access(DEX_PATH, F_OK) != 0) {
            LOGE("Can't access %s.", DEX_PATH);
            return false;
        }
        jclass classloaderClass = env->FindClass("java/lang/ClassLoader");
        jmethodID getsysClassloaderMethod = env->GetStaticMethodID(classloaderClass, "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
        jobject loader = env->CallStaticObjectMethod(classloaderClass, getsysClassloaderMethod);
        jclass dexLoaderClass = env->FindClass("dalvik/system/DexClassLoader");
        jmethodID initDexLoaderMethod = env->GetMethodID(dexLoaderClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
        jobject dexLoader = env->NewObject(dexLoaderClass, initDexLoaderMethod,
                                           env->NewStringUTF(DEX_PATH),
                                           env->NewStringUTF((appDataDir + "/code_cache").c_str()),
                                           NULL, loader);
        jmethodID loadclassMethod = env->GetMethodID(dexLoaderClass, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");

        jclass javaClientClass = (jclass)env->CallObjectMethod(dexLoader, loadclassMethod, env->NewStringUTF(MAIN_CLASS));
        jmethodID mainMethod = env->GetStaticMethodID(javaClientClass, MAIN_METHOD, "(Ljava/lang/String;)Z");
        if (mainMethod == NULL) {
            LOGE("Main method %s.%s not found.", MAIN_CLASS, MAIN_METHOD);
            return false;
        }
        jboolean hasHook = env->CallStaticBooleanMethod(javaClientClass, mainMethod, env->NewStringUTF(niceName));

        jint ret = env->RegisterNatives(javaClientClass, gJni_Methods_table, lsplant::arraysize(gJni_Methods_table));
        if (ret != 0) {
            LOGE("RegisterNatives ret %d", ret);
        }
        return true;
    }
};

// Register our module class
REGISTER_ZYGISK_MODULE(MyModule)


