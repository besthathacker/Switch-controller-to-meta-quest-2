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

    private TextView statusText, connectionInfo, batteryInfo, joystickInfo, buttonsInfo;

    static {
        System.loadLibrary("native-lib");
    }

    public native void updateHands(float[] leftData, float[] rightData, float dt);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        connectionInfo = findViewById(R.id.connectionInfo);
        batteryInfo = findViewById(R.id.batteryInfo);
        joystickInfo = findViewById(R.id.joystickInfo);
        buttonsInfo = findViewById(R.id.buttonsInfo);

        statusText.setText("Scanning for Joy-Cons…");

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

        bleBridge = new BleBridge(this, (imu, buttons, handedness) -> {
            float dt = 1.0f / 60.0f;

            if ("left".equals(handedness)) {
                updateHands(imu, null, dt);
            } else {
                updateHands(null, imu, dt);
            }

            ShizukuHelper.injectVRControllerEvent(imu, buttons, handedness);

            runOnUiThread(() -> {
                statusText.setText("Connected: " + handedness + " Joy-Con");

                connectionInfo.setText(
                        "IMU:\n" +
                                "ax=" + imu[0] + "\n" +
                                "ay=" + imu[1] + "\n" +
                                "az=" + imu[2] + "\n" +
                                "gx=" + imu[3] + "\n" +
                                "gy=" + imu[4] + "\n" +
                                "gz=" + imu[5]
                );

                batteryInfo.setText("Battery: " + imu[6] + "%");

                joystickInfo.setText(
                        "Joystick:\nX=" + imu[7] + "\nY=" + imu[8]
                );

                buttonsInfo.setText("Buttons: " + buttons);
            });
        });

        bleBridge.startScan();
    }
}
