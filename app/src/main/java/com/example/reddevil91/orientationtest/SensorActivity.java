package com.example.reddevil91.orientationtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private float acc_values[], mag_values[];
    private float[] orientation = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Intent i = getIntent();
        final String address = i.getStringExtra(MainActivity.EXTRA_ADDRESS);

        Button start_btn = findViewById(R.id.bluetooth_btn);

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread connect_thread = new ConnectThread(address);
                connect_thread.start();
//                try {
//                    connect_thread.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });
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
        // do nothing
    }

    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        ConnectThread(String address) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = adapter.getRemoteDevice(address);
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(uuid);
//                tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e("TAG", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.e("TAG", "Could not connect!", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("TAG", "Could not close the client socket", closeException);
                }
                return;
            }
            Log.i("TAG", "Connected");
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }
    }

    public void manageMyConnectedSocket(BluetoothSocket socket){
        try {
            socket.close();
        } catch (IOException e) {
            Log.e("TAG", "Could not close the connect socket", e);
        }
        Log.i("TAG", "Connection closed");
    }

}
