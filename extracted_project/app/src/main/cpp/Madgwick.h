#pragma once

struct Quaternion { float w, x, y, z; };

class MadgwickAHRS {
public:
    float beta = 0.1f;
    Quaternion q{1,0,0,0};
    void Update(float gx, float gy, float gz, float ax, float ay, float az, float dt);
};
