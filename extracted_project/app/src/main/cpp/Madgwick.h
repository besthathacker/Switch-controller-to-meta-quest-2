#pragma once
#include <cmath>

struct Quaternion { float w, x, y, z; };

class MadgwickAHRS {
public:
    float beta = 0.1f;       // Algorithm gain
    Quaternion q{1.0f, 0.0f, 0.0f, 0.0f};

    void Update(float gx, float gy, float gz,
                float ax, float ay, float az,
                float dt);
};
