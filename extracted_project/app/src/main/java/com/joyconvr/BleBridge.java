package com.joyconvr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.util.Log;
import android.content.Context;

public class BleBridge {
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private static final String TAG = "JoyConVR_BLE";

    public BleBridge(Context context) {
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void startScan() {
        adapter.startLeScan((device, rssi, scanRecord) -> {
            Log.i(TAG, "Found device: " + device.getName());
            if (device.getName() != null && (device.getName().contains("Joy-Con L") || device.getName().contains("Joy-Con R"))) {
                device.connectGatt(null, false, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt g, int status, int newState) {
                        Log.i(TAG, "Connected to " + device.getName());
                    }
                });
            }
        });
    }
}
