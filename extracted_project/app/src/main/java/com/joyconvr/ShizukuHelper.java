package com.joyconvr;

import android.os.Build;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import dev.rikka.shizuku.Shizuku;
import dev.rikka.shizuku.Shizuku.OnRequestPermissionResultListener;

import java.lang.reflect.Method;

public class ShizukuHelper {
    private static final String TAG = "ShizukuHelper";

    public static void checkPermission(@NonNull android.content.Context context) {
        if (!Shizuku.isApiAvailable()) {
            Log.e(TAG, "Shizuku API not available");
            return;
        }

        if (!Shizuku.isAuthorized()) {
            Shizuku.requestPermission(1001, new OnRequestPermissionResultListener() {
                @Override
                public void onRequestPermissionResult(int requestCode, boolean granted) {
                    if (granted) {
                        Log.i(TAG, "Shizuku permission granted");
                    } else {
                        Log.e(TAG, "Shizuku permission denied");
                    }
                }
            });
        } else {
            Log.i(TAG, "Shizuku already authorized");
        }
    }

    public static void injectVRControllerEvent(float[] imu, String buttons, String handedness) {
        try {
            Object inputService = Shizuku.SystemService.get("input");
            if (inputService == null) {
                Log.e(TAG, "Input service not available");
                return;
            }

            Method injectMethod = inputService.getClass().getMethod("injectInputEvent", android.view.InputEvent.class, int.class);

            // Simple example: create a MotionEvent for joystick
            long now = System.currentTimeMillis();
            float x = imu[7]; // joystick X
            float y = imu[8]; // joystick Y

            MotionEvent evt = MotionEvent.obtain(
                    now, now,
                    MotionEvent.ACTION_MOVE,
                    x, y,
                    0
            );
            evt.setSource(InputDevice.SOURCE_JOYSTICK);

            injectMethod.invoke(inputService, evt, 0);

            // Map buttons to KeyEvents
            if (buttons.contains("A")) {
                KeyEvent keyEvent = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BUTTON_A, 0);
                injectMethod.invoke(inputService, keyEvent, 0);
                KeyEvent keyEventUp = new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BUTTON_A, 0);
                injectMethod.invoke(inputService, keyEventUp, 0);
            }
            if (buttons.contains("B")) {
                KeyEvent keyEvent = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BUTTON_B, 0);
                injectMethod.invoke(inputService, keyEvent, 0);
                KeyEvent keyEventUp = new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BUTTON_B, 0);
                injectMethod.invoke(inputService, keyEventUp, 0);
            }

            Log.i(TAG, "Injected VR event for " + handedness + " Joy-Con: " + buttons);

        } catch (Exception e) {
            Log.e(TAG, "Failed to inject VR event", e);
        }
    }
}
