package com.example.reddevil91.orientationtest;

/**
 * Created by RedDevil91 on 2018. 03. 23..
 * Class for device data
 * Bluetooth name and address
 */

public class DetectedDevice {
    private String bluetoothDeviceName;
    private String bluetoothDeviceAddress;
    private boolean bluetoothDevicePaired;

    DetectedDevice(String deviceName, String deviceAddress, boolean paired){
        bluetoothDeviceName = deviceName;
        bluetoothDeviceAddress = deviceAddress;
        bluetoothDevicePaired = paired;
    }

    public String getBluetoothDeviceName() {
        return bluetoothDeviceName;
    }

    public String getBluetoothDeviceAddress() {
        return bluetoothDeviceAddress;
    }

    public boolean isBluetoothDevicePaired() {
        return bluetoothDevicePaired;
    }
}
