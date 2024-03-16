package com.example.osmzhttpserver;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SocketServer s;
    private TextView messages_list;
    DataProvider dataProvider;
    SensorsService sensorsService;
    GPSService gpsService;
    SensorManager sensorManager;
    private Sensor mLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing services
        gpsService = new GPSService(this);
        dataProvider = new DataProvider(this, gpsService);
        gpsService.dataProvider = dataProvider;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorsService = new SensorsService(dataProvider);
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        Button btn1 = findViewById(R.id.button1);
        Button btn2 = findViewById(R.id.button2);
        messages_list = findViewById(R.id.messages_list);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    private void getPermissions() {
        if (Environment.isExternalStorageManager()) {
            String sdPath = Environment.getExternalStorageDirectory().getPath();
            File file = new File(sdPath + "/website/index.html");
            if (file.exists()) {
                s = new SocketServer(this, messages_list, dataProvider);
                s.start();
            } else {
                Log.d("SERVER", "index.html does not exist");
            }
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1) {
            getPermissions();
        }
        if (v.getId() == R.id.button2) {
            s.close();
            try {
                s.join();
            } catch (InterruptedException e) {
                StackTraceElement[] trace = e.getStackTrace();
                for (int i = 0; i < trace.length; i++) {
                    Log.d("SERVER", "SocketServer(" + e.getStackTrace()[1].getLineNumber() + "): " + Arrays.toString(trace));
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int delay = 1000000; // Send sensor data every 1 seconds
        sensorManager.registerListener(sensorsService, mLight, delay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorsService);
    }
}