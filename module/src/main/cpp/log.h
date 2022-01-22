//
// Created by smallville on 2022-01-16.
//

#ifndef LTWEAKSMAGI_LOG_H
#define LTWEAKSMAGI_LOG_H

#include <android/log.h>

#ifndef LOG_TAG
#define LOG_TAG "LTweaks"
#endif

#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#endif //LTWEAKSMAGI_LOG_H
