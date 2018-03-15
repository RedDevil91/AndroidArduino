package com.example.reddevil91.orientationtest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private float orientation[];
    private Listener acc_listener, mag_listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orientation);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acc_listener = new Listener(sensorManager, Sensor.TYPE_ACCELEROMETER);
        mag_listener = new Listener(sensorManager, Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TextView roll = findViewById(R.id.roll_value);
        TextView pitch = findViewById(R.id.pitch_value);
        TextView yaw = findViewById(R.id.yaw_value);

        roll.setText("Test");
        pitch.setText("Test");
        yaw.setText("Test");

        return super.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(acc_listener);
        sensorManager.unregisterListener(mag_listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(acc_listener, acc_listener.sensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(mag_listener, mag_listener.sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
