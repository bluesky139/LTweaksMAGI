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

using zygisk::Api;
using zygisk::AppSpecializeArgs;
using zygisk::ServerSpecializeArgs;

const char* FOLDER = "/data/local/LTweaksMAGI";
const char* DEX_PATH = "/data/local/LTweaksMAGI/MAGI.dex";
const char* MAIN_CLASS = "li.lingfeng.magi.Loader";
const char* MAIN_METHOD = "load";

class MyModule : public zygisk::ModuleBase {
public:
    void onLoad(Api* api, JNIEnv* env) override {
        this->api = api;
        this->env = env;
    }

    void postAppSpecialize(const AppSpecializeArgs* args) override {
        std::string niceName = jstringToString(env, args->nice_name);
        if (niceName.size() > 100) {
            LOGW("Nice name too long, %s", niceName.c_str());
            return;
        }
        char prefPath[150] = {0};
        sprintf(prefPath, "%s/%s.pref", FOLDER, niceName.c_str());
        if (access(prefPath, F_OK) == 0) {
            LOGD("Load dex for %s", niceName.c_str());
            std::string appDataDir = jstringToString(env, args->app_data_dir);
            loadDex(env, niceName.c_str(), appDataDir);
        }

        // Since we do not hook any functions, we should let Zygisk dlclose ourselves
        api->setOption(zygisk::Option::DLCLOSE_MODULE_LIBRARY);
    }

    void postServerSpecialize(const ServerSpecializeArgs *args) override {
        std::string appDataDir;
        loadDex(env, "android", appDataDir);

        // Since we do not hook any functions, we should let Zygisk dlclose ourselves
        api->setOption(zygisk::Option::DLCLOSE_MODULE_LIBRARY);
    }

private:
    Api* api;
    JNIEnv* env;

    std::string jstringToString(JNIEnv* env, jstring& jstr) {
        if (jstr) {
            const char* ch = env->GetStringUTFChars(jstr, 0);
            std::string str = std::string(ch);
            env->ReleaseStringUTFChars(jstr, ch);
            return str;
        }
        return std::string();
    }

    void loadDex(JNIEnv* env, const char* niceName, std::string& appDataDir) {
        if (access(DEX_PATH, F_OK) != 0) {
            LOGE("Can't access %s.", DEX_PATH);
            return;
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
        jmethodID mainMethod = env->GetStaticMethodID(javaClientClass, MAIN_METHOD, "(Ljava/lang/String;)V");
        if (mainMethod == NULL) {
            LOGE("Main method %s.%s not found.", MAIN_CLASS, MAIN_METHOD);
            return;
        }
        env->CallStaticVoidMethod(javaClientClass, mainMethod, env->NewStringUTF(niceName));
    }
};

// Register our module class
REGISTER_ZYGISK_MODULE(MyModule)
