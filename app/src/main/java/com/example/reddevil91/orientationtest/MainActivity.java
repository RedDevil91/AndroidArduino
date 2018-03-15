package com.example.reddevil91.orientationtest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private float acc_values[], mag_values[];
    private float[] orientation = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orientation);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
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
