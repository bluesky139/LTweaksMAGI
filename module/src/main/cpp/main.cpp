#include <jni.h>
#include <sys/types.h>
#include <riru.h>
#include <malloc.h>
#include <cstring>
#include <string>
#include <config.h>
#include <unistd.h>
#include "log.h"

const char* FOLDER = "/data/local/LTweaksMAGI";
const char* DEX_PATH = "/data/local/LTweaksMAGI/MAGI.dex";
const char* MAIN_CLASS = "li.lingfeng.magi.Loader";
const char* MAIN_METHOD = "load";
std::string sNiceName;
std::string sAppDataDir;

std::string jstringToString(JNIEnv * env, jstring jstr){
    const char* str = env->GetStringUTFChars(jstr, 0);
    return jstr ? std::string(str) : std::string();
}

void loadDex(JNIEnv* env, const char* niceName) {
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
                                       env->NewStringUTF((sAppDataDir + "/code_cache").c_str()),
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

static void forkAndSpecializePre(
        JNIEnv *env, jclass clazz, jint *uid, jint *gid, jintArray *gids, jint *runtimeFlags,
        jobjectArray *rlimits, jint *mountExternal, jstring *seInfo, jstring *niceName,
        jintArray *fdsToClose, jintArray *fdsToIgnore, jboolean *is_child_zygote,
        jstring *instructionSet, jstring *appDataDir, jboolean *isTopApp, jobjectArray *pkgDataInfoList,
        jobjectArray *whitelistedDataInfoList, jboolean *bindMountAppDataDirs, jboolean *bindMountAppStorageDirs) {
    // Called "before" com_android_internal_os_Zygote_nativeForkAndSpecialize in frameworks/base/core/jni/com_android_internal_os_Zygote.cpp
    // Parameters are pointers, you can change the value of them if you want
    // Some parameters are not exist is older Android versions, in this case, they are null or 0

    sNiceName = jstringToString(env, *niceName);
    sAppDataDir = jstringToString(env, *appDataDir);
}

static void forkAndSpecializePost(JNIEnv *env, jclass clazz, jint res) {
    // Called "after" com_android_internal_os_Zygote_nativeForkAndSpecialize in frameworks/base/core/jni/com_android_internal_os_Zygote.cpp
    // "res" is the return value of com_android_internal_os_Zygote_nativeForkAndSpecialize

    if (res == 0) {
        // In app process

        // When unload allowed is true, the module will be unloaded (dlclose) by Riru
        // If this modules has hooks installed, DONOT set it to true, or there will be SIGSEGV
        // This value will be automatically reset to false before the "pre" function is called
        riru_set_unload_allowed(true);

        char prefPath[100] = {0};
        sprintf(prefPath, "%s/%s.pref", FOLDER, sNiceName.c_str());
        if (access(prefPath, F_OK) == 0) {
            LOGD("Load dex for %s", sNiceName.c_str());
            loadDex(env, sNiceName.c_str());
        }
    } else {
        // In zygote process
    }
}

static void specializeAppProcessPre(
        JNIEnv *env, jclass clazz, jint *uid, jint *gid, jintArray *gids, jint *runtimeFlags,
        jobjectArray *rlimits, jint *mountExternal, jstring *seInfo, jstring *niceName,
        jboolean *startChildZygote, jstring *instructionSet, jstring *appDataDir,
        jboolean *isTopApp, jobjectArray *pkgDataInfoList, jobjectArray *whitelistedDataInfoList,
        jboolean *bindMountAppDataDirs, jboolean *bindMountAppStorageDirs) {
    // Called "before" com_android_internal_os_Zygote_nativeSpecializeAppProcess in frameworks/base/core/jni/com_android_internal_os_Zygote.cpp
    // Parameters are pointers, you can change the value of them if you want
    // Some parameters are not exist is older Android versions, in this case, they are null or 0
}

static void specializeAppProcessPost(
        JNIEnv *env, jclass clazz) {
    // Called "after" com_android_internal_os_Zygote_nativeSpecializeAppProcess in frameworks/base/core/jni/com_android_internal_os_Zygote.cpp

    // When unload allowed is true, the module will be unloaded (dlclose) by Riru
    // If this modules has hooks installed, DONOT set it to true, or there will be SIGSEGV
    // This value will be automatically reset to false before the "pre" function is called
    riru_set_unload_allowed(true);
}

static void forkSystemServerPre(
        JNIEnv *env, jclass clazz, uid_t *uid, gid_t *gid, jintArray *gids, jint *runtimeFlags,
        jobjectArray *rlimits, jlong *permittedCapabilities, jlong *effectiveCapabilities) {
    // Called "before" com_android_internal_os_Zygote_forkSystemServer in frameworks/base/core/jni/com_android_internal_os_Zygote.cpp
    // Parameters are pointers, you can change the value of them if you want
    // Some parameters are not exist is older Android versions, in this case, they are null or 0
}

static void forkSystemServerPost(JNIEnv *env, jclass clazz, jint res) {
    // Called "after" com_android_internal_os_Zygote_forkSystemServer in frameworks/base/core/jni/com_android_internal_os_Zygote.cpp

    if (res == 0) {
        // In system server process
    } else {
        // In zygote process
    }
}

static void onModuleLoaded() {
    // Called when this library is loaded and "hidden" by Riru (see Riru's hide.cpp)

    // If you want to use threads, start them here rather than the constructors
    // __attribute__((constructor)) or constructors of static variables,
    // or the "hide" will cause SIGSEGV
}

extern "C" {

int riru_api_version;
const char *riru_magisk_module_path = nullptr;
int *riru_allow_unload = nullptr;

static auto module = RiruVersionedModuleInfo{
        .moduleApiVersion = riru::moduleApiVersion,
        .moduleInfo= RiruModuleInfo{
                .supportHide = true,
                .version = riru::moduleVersionCode,
                .versionName = riru::moduleVersionName,
                .onModuleLoaded = onModuleLoaded,
                .forkAndSpecializePre = forkAndSpecializePre,
                .forkAndSpecializePost = forkAndSpecializePost,
                .forkSystemServerPre = forkSystemServerPre,
                .forkSystemServerPost = forkSystemServerPost,
                .specializeAppProcessPre = specializeAppProcessPre,
                .specializeAppProcessPost = specializeAppProcessPost
        }
};

RiruVersionedModuleInfo *init(Riru *riru) {
    auto core_max_api_version = riru->riruApiVersion;
    riru_api_version = core_max_api_version <= riru::moduleApiVersion ? core_max_api_version : riru::moduleApiVersion;
    module.moduleApiVersion = riru_api_version;

    riru_magisk_module_path = strdup(riru->magiskModulePath);
    if (riru_api_version >= 25) {
        riru_allow_unload = riru->allowUnload;
    }
    return &module;
}
}
