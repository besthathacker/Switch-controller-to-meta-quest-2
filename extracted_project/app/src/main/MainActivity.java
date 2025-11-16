package com.joyconvr;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "JoyConVR";
    private BluetoothAdapter bluetoothAdapter;
    private BleBridge bleBridge;

    static {
        System.loadLibrary("native-lib");
    }
    public native void updateHands(float[] leftData, float[] rightData, float dt);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ShizukuHelper.checkPermission(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device does not support Bluetooth");
            finish();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            }, 1);
        }

        bleBridge = new BleBridge(this, (imu, buttons, handedness) -> {
            float dt = 1.0f / 60.0f;
            if ("left".equals(handedness)) {
                updateHands(imu, null, dt);
            } else {
                updateHands(null, imu, dt);
            }
            ShizukuHelper.injectVRControllerEvent(imu, buttons, handedness);
        });

        bleBridge.startScan();
    }
}