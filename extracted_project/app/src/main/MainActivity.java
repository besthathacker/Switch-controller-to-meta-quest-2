package com.joyconvr;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "JoyConVR";
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device does not support Bluetooth");
            finish();
            return;
        }

        // Request BLE permissions for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 1);
        }

        startDynamicJoyConScan();
    }

    private void startDynamicJoyConScan() {
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(scanCallback);
        Log.i(TAG, "Scanning dynamically for Joy-Cons...");
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            String mac = device.getAddress();

            if (name != null && name.contains("Joy-Con")) {
                Log.i(TAG, "Found Joy-Con: " + name + " - UDID/MAC: " + mac);

                // Connect to GATT to inspect services
                device.connectGatt(MainActivity.this, false, new BluetoothGattCallback() {
                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        List<BluetoothGattService> services = gatt.getServices();
                        Log.i(TAG, "Discovered services for " + name + ":");
                        for (BluetoothGattService service : services) {
                            UUID serviceUUID = service.getUuid();
                            Log.i(TAG, "Service UUID: " + serviceUUID.toString());
                        }
                        // You can now subscribe to IMU/button characteristics here
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, android.bluetooth.BluetoothGattCharacteristic characteristic) {
                        // Read IMU or button data here
                    }
                });
            }
        }
    };
}
