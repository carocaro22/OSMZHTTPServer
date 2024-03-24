package com.example.osmzhttpserver;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.osmzhttpserver.services.GPSService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataProvider {
    GPSService gpsService;

    DataProvider(Context context, GPSService gps) {
        this.gpsService = gps;
    }

    String createJsonElement(DataModel model) {
        Log.d("SENSOR", "6. Creating json element...");
        Log.d("SENSOR", "Latitude: " + model.latitude);
        Log.d("SENSOR", "Longitude: " + model.longitude);
        Log.d("SENSOR", "Light: " + model.light[0]);
        try {
            JSONObject json = new JSONObject();
            JSONObject location = new JSONObject();
            location.put("latitude", model.latitude);
            location.put("longitude", model.longitude);
            json.put("location", location);
            json.put("light", model.light[0]);
            Log.d("SENSOR", "7. Successfully created JSON");
            return json.toString();
        } catch (JSONException e) {
            Log.d("SENSOR", "Could not create JSON");
            return null;
        }
    }
    // Call from Sensors Service
    public void getGpsData(DataModel model) {
        gpsService.getLastLocation(model);
    }

    // Call from GPSService
    public void writeToJsonFile(DataModel model) {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        File sensorData = new File(sdPath + "/website/streams/", "telemetry.json");
        if (sensorData.exists()) {
            Log.d("SENSOR", "5. trying to write to json file...");
            String json = createJsonElement(model);
            try (FileWriter writer = new FileWriter(sensorData)) {
                writer.write(json);
                Log.d("SENSOR", "8. Successfully wrote to file");
            } catch (IOException error) {
                Log.d("SENSOR", "Could not write to telemetry file");
                Log.d("SENSOR", String.valueOf(error));
            }
        } else {
            try {
                sensorData.createNewFile();
                Log.d("SENSOR", "4. New file created");
                writeToJsonFile(model);
            } catch (IOException error) {
                Log.d("SENSOR", "4. Could not create file");
                Log.d("SENSOR", String.valueOf(error));
            }
        }
    }
}
