package com.example.osmzhttpserver;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class GPSService {
    private final FusedLocationProviderClient fusedLocationProviderClient;
    Context context;
    DataProvider dataProvider;

    public GPSService(Context context) {
        this.context = context;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getLastLocation(DataModel model) {
        Log.d("SENSOR", "2. Checking location permissions...");
        if (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("SENSOR", "Permissions not granted. Please grant permissions!");
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.d("SENSOR", "3. Writing gps coordinates...");
                            Log.d("SENSOR", "Latitude: " + location.getLatitude());
                            Log.d("SENSOR", "Longitude: " + location.getLongitude());
                            model.latitude = location.getLatitude();
                            model.longitude = location.getLongitude();
                            dataProvider.writeToJsonFile(model);
                        } else {
                            Log.d("SENSOR", "Location Unknown");
                        }
                    }
                });
    }
}
