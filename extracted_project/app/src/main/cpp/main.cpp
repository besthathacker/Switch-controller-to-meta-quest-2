#include <openxr/openxr.h>
#include <android/log.h>
#include "Madgwick.h"

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "JoyConVR", __VA_ARGS__))

MadgwickAHRS leftHandFilter;
MadgwickAHRS rightHandFilter;

extern "C"
JNIEXPORT void JNICALL
Java_com_joyconvr_BleBridge_updateHands(JNIEnv *env, jobject obj, jfloatArray leftData, jfloatArray rightData, jfloat dt) {
    jfloat* l = env->GetFloatArrayElements(leftData, 0);
    jfloat* r = env->GetFloatArrayElements(rightData, 0);

    leftHandFilter.Update(l[0], l[1], l[2], l[3], l[4], l[5], dt);
    rightHandFilter.Update(r[0], r[1], r[2], r[3], r[4], r[5], dt);

    env->ReleaseFloatArrayElements(leftData, l, 0);
    env->ReleaseFloatArrayElements(rightData, r, 0);
}
