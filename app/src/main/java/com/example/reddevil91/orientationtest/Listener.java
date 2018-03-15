package com.example.reddevil91.orientationtest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by RedDevil91 on 2018. 03. 15..
 * Sensor event listener class
 */

public class Listener implements SensorEventListener{
    Sensor sensor;
    float values[];

    Listener(SensorManager sensorManager, int sensorType){
        sensor = sensorManager.getDefaultSensor(sensorType);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        values = sensorEvent.values;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // do nothing
    }
}
