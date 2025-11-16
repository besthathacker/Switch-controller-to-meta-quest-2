package com.joyconvr;

import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import dev.rikka.shizuku.Shizuku;

public class ShizukuHelper {
    private static final String TAG = "JoyConVR_Shizuku";

    public static boolean checkPermission(Context ctx) {
        if (!Shizuku.isApiAvailable()) {
            Log.e(TAG, "Shizuku API not available. Cannot use privileged operations on Quest.");
            return false;
        }
        if (!Shizuku.isAuthorized()) {
            Shizuku.requestPermission(1001, (requestCode, grantResult) -> {
                if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Shizuku permission granted!");
                } else {
                    Log.e(TAG, "Shizuku permission denied.");
                }
            });
            return false;
        }
        return true;
    }

    public static IBinder getInputManagerBinder() {
        return Shizuku.SystemService.get("input");
    }

    public static void injectVRControllerEvent(float[] orientation, float[] buttons, String handedness) {
        IBinder binder = getInputManagerBinder();
        if (binder == null) {
            Log.e(TAG, "InputManager not available via Shizuku.");
            return;
        }
        try {
            android.hardware.input.IInputManager inputManager = android.hardware.input.IInputManager.Stub.asInterface(binder);
            long now = System.currentTimeMillis();
            MotionEvent event = MotionEvent.obtain(
                now, now,
                buttons[0] > 0 ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP,
                orientation[0], orientation[1], 0
            );
            inputManager.injectInputEvent(event, 0);
            Log.i(TAG, "Injected VR event: " + handedness + ", orientation=" + orientation[0] + "," + orientation[1] + "," + orientation[2]);
            event.recycle();
        } catch (Exception e) {
            Log.e(TAG, "Failed to inject VR event: " + e);
        }
    }
}