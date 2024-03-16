package com.example.osmzhttpserver;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

@SuppressLint("Registered")
public class SensorsService implements SensorEventListener {
    DataProvider dataProvider;

    SensorsService(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        Log.d("SENSOR", "Sensor service was created");
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        Log.d("SENSOR", "1. Sensor data: " + event.values[0]);
        DataModel model = new DataModel();
        model.light = event.values;
        dataProvider.getGpsData(model);
    }
}
