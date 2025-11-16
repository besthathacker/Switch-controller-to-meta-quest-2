#include <jni.h>
#include "Madgwick.h"

MadgwickAHRS leftHandFilter, rightHandFilter;

extern "C"
JNIEXPORT void JNICALL
Java_com_joyconvr_MainActivity_updateHands(JNIEnv *env, jobject obj, jfloatArray leftData, jfloatArray rightData, jfloat dt) {
    if (leftData != nullptr) {
        jfloat *l = env->GetFloatArrayElements(leftData, 0);
        leftHandFilter.Update(l[0], l[1], l[2], l[3], l[4], l[5], dt);
        env->ReleaseFloatArrayElements(leftData, l, 0);
    }
    if (rightData != nullptr) {
        jfloat *r = env->GetFloatArrayElements(rightData, 0);
        rightHandFilter.Update(r[0], r[1], r[2], r[3], r[4], r[5], dt);
        env->ReleaseFloatArrayElements(rightData, r, 0);
    }
}