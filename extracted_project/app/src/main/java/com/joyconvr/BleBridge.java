package com.joyconvr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

public class BleBridge {
    private BluetoothAdapter adapter;
    private static final String TAG = "JoyConVR_BLE";
    private JoyConDataListener listener;

    public interface JoyConDataListener {
        void onJoyConData(float[] imu, float[] buttons, String handedness);
    }

    public BleBridge(Context context, JoyConDataListener listener) {
        adapter = BluetoothAdapter.getDefaultAdapter();
        this.listener = listener;
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
                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        float[] imu = parseIMU(characteristic);
                        float[] buttons = parseButtons(characteristic);
                        String handedness = device.getName().contains("L") ? "left" : "right";
                        listener.onJoyConData(imu, buttons, handedness);
                    }
                });
            }
        });
    }

    private float[] parseIMU(BluetoothGattCharacteristic c) {
        byte[] d = c.getValue();
        float[] imu = new float[6];
        for (int i = 0; i < 6 && (i*2+1) < d.length; i++) {
            int val = ((d[i*2+1] << 8) | (d[i*2] & 0xFF));
            imu[i] = (float)val;
        }
        return imu;
    }
    private float[] parseButtons(BluetoothGattCharacteristic c) {
        byte[] d = c.getValue();
        float[] btn = new float[8];
        if (d.length > 12) {
            byte mask = d[12];
            for (int i = 0; i < 8; i++) {
                btn[i] = ((mask >> i) & 0x01);
            }
        }
        return btn;
    }
}