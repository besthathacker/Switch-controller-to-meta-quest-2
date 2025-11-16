#include "Madgwick.h"

// Fast inverse square root helper
static float invSqrt(float x) {
    return 1.0f / std::sqrt(x);
}

void MadgwickAHRS::Update(float gx, float gy, float gz,
                          float ax, float ay, float az,
                          float dt) {
    float q1 = q.w, q2 = q.x, q3 = q.y, q4 = q.z;

    // Normalize accelerometer measurement
    float norm = invSqrt(ax*ax + ay*ay + az*az);
    if (norm == 0.0f) return;
    ax *= norm;
    ay *= norm;
    az *= norm;

    // Gradient descent algorithm corrective step
    float f1 = 2*(q2*q4 - q1*q3) - ax;
    float f2 = 2*(q1*q2 + q3*q4) - ay;
    float f3 = 2*(0.5f - q2*q2 - q3*q3) - az;
    float J_11or24 = 2*q3;
    float J_12or23 = 2*q4;
    float J_13or22 = 2*q1;
    float J_14or21 = 2*q2;
    float J_32 = 2*J_14or21;
    float J_33 = 2*J_11or24;

    float grad1 = J_14or21*f2 - J_11or24*f1;
    float grad2 = J_12or23*f1 + J_13or22*f2 - J_32*f3;
    float grad3 = J_12or23*f2 - J_33*f3 - J_13or22*f1;
    float grad4 = J_14or21*f1 + J_11or24*f2;

    norm = invSqrt(grad1*grad1 + grad2*grad2 + grad3*grad3 + grad4*grad4);
    grad1 *= norm; grad2 *= norm; grad3 *= norm; grad4 *= norm;

    q1 += dt * (0.5f * (-q2*gx - q3*gy - q4*gz) - beta * grad1);
    q2 += dt * (0.5f * (q1*gx + q3*gz - q4*gy) - beta * grad2);
    q3 += dt * (0.5f * (q1*gy - q2*gz + q4*gx) - beta * grad3);
    q4 += dt * (0.5f * (q1*gz + q2*gy - q3*gx) - beta * grad4);

    norm = invSqrt(q1*q1 + q2*q2 + q3*q3 + q4*q4);
    q.w = q1 * norm;
    q.x = q2 * norm;
    q.y = q3 * norm;
    q.z = q4 * norm;
}