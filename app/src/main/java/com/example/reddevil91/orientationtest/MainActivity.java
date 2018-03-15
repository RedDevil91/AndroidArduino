package com.example.reddevil91.orientationtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private BluetoothAdapter bluetoothAdapter;
    private float acc_values[], mag_values[];
    private float[] orientation = new float[3];
    private static final int REQUEST_ENABLE_BT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orientation);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            getPairedDevices();
        }
    }

    private void getPairedDevices(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Toast.makeText(getApplicationContext(), "Paired with device: " + deviceName, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Failed to find paired devices!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK){
            Toast.makeText(getApplicationContext(), "Bluetooth enabled!", Toast.LENGTH_SHORT).show();
            getPairedDevices();
        }
        else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Bluetooth is disabled!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acc_values = sensorEvent.values;
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mag_values = sensorEvent.values;
        }

        if (acc_values != null && mag_values != null) {
            float R_M[] = new float[9];
            float I_M[] = new float[9];
            sensorManager.getRotationMatrix(R_M, I_M, acc_values, mag_values);
            sensorManager.getOrientation(R_M, orientation);

            TextView roll = findViewById(R.id.roll);
            TextView pitch = findViewById(R.id.pitch);
            TextView yaw = findViewById(R.id.yaw);

            roll.setText(String.format(getString(R.string.roll), Math.toDegrees(orientation[1])));
            pitch.setText(String.format(getString(R.string.pitch), Math.toDegrees(orientation[2])));
            yaw.setText(String.format(getString(R.string.yaw), Math.toDegrees(orientation[0])));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing
    }
}
