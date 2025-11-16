package com.joyconvr;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "JoyConVR";
    private BluetoothAdapter bluetoothAdapter;
    private BleBridge bleBridge;
    private TextView statusText, leftControllerText, rightControllerText, orientationText;
    private Button scanButton;

    static {
        System.loadLibrary("native-lib");
    }
    public native void updateHands(float[] leftData, float[] rightData, float dt);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        leftControllerText = findViewById(R.id.leftControllerText);
        rightControllerText = findViewById(R.id.rightControllerText);
        orientationText = findViewById(R.id.orientationText);
        scanButton = findViewById(R.id.scanButton);

        ShizukuHelper.checkPermission(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            statusText.setText("Status: Device does not support Bluetooth");
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
            runOnUiThread(() -> {
                if ("left".equals(handedness)) {
                    leftControllerText.setText("Left Joy-Con: Connected");
                } else {
                    rightControllerText.setText("Right Joy-Con: Connected");
                }
                orientationText.setText(
                    String.format("%s orientation: gx=%.2f gy=%.2f gz=%.2f ax=%.2f ay=%.2f az=%.2f",
                        handedness,
                        imu[0], imu[1], imu[2],
                        imu[3], imu[4], imu[5])
                );
                statusText.setText("Status: Joy-Con " + handedness + " data received");
            });

            if ("left".equals(handedness)) {
                updateHands(imu, null, dt);
            } else {
                updateHands(null, imu, dt);
            }
            ShizukuHelper.injectVRControllerEvent(imu, buttons, handedness);
        });

        scanButton.setOnClickListener(v -> {
            statusText.setText("Status: Scanning...");
            bleBridge.startScan();
        });
    }
}