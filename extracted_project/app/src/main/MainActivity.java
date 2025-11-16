package com.joyconvr;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "JoyConVR";

    private BluetoothAdapter bluetoothAdapter;
    private BleBridge bleBridge;

    // UI elements
    private TextView statusText, connectionInfo, batteryInfo, joystickInfo, buttonsInfo;

    static {
        System.loadLibrary("native-lib");
    }

    public native void updateHands(float[] leftData, float[] rightData, float dt);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ----- UI INIT -----
        statusText = findViewById(R.id.statusText);
        connectionInfo = findViewById(R.id.connectionInfo);
        batteryInfo = findViewById(R.id.batteryInfo);
        joystickInfo = findViewById(R.id.joystickInfo);
        buttonsInfo = findViewById(R.id.buttonsInfo);

        statusText.setText("Scanning for Joy‑Cons…");

        // ----- PERMISSIONS -----
        ShizukuHelper.checkPermission(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device does not support Bluetooth");
            statusText.setText("Bluetooth not supported.");
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            }, 1);
        }

        // ----- BLE BRIDGE -----
        bleBridge = new BleBridge(this, (imu, buttons, handedness) -> {

            float dt = 1.0f / 60.0f;

            // Native VR hand update
            if ("left".equals(handedness)) {
                updateHands(imu, null, dt);
            } else {
                updateHands(null, imu, dt);
            }

            // Inject events into VR overlay via Shizuku
            ShizukuHelper.injectVRControllerEvent(imu, buttons, handedness);

            // ----- UI UPDATE -----
            runOnUiThread(() -> {
                statusText.setText("Connected: " + handedness + " Joy‑Con");

                // IMU: accel + gyro (first 6 floats)
                connectionInfo.setText(
                    "IMU:\n" +
                    "ax=" + imu[0] + "\n" +
                    "ay=" + imu[1] + "\n" +
                    "az=" + imu[2] + "\n" +
                    "gx=" + imu[3] + "\n" +
                    "gy=" + imu[4] + "\n" +
                    "gz=" + imu[5]
                );

                // Battery (Joy‑Cons encode as bitmask, but BleBridge simplifies → direct int)
                batteryInfo.setText("Battery: " + imu[6] + "%");

                // Joystick: imu[7], imu[8]
                joystickInfo.setText(
                    "Joystick:\nX=" + imu[7] + "\nY=" + imu[8]
                );

                // Buttons → already string from BleBridge
                buttonsInfo.setText("Buttons: " + buttons);
            });
        });

        // Start BLE scanning
        bleBridge.startScan();
    }
}
