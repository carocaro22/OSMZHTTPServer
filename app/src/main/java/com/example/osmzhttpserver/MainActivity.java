package com.example.osmzhttpserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SocketServer s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

    }
    
    private void getPermissions() {
        if (Environment.isExternalStorageManager()) {
            String sdPath = Environment.getExternalStorageDirectory().getPath();
            File file = new File(sdPath + "/website/index.html");
            if (file.exists()) {
                s = new SocketServer(this);
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
                e.printStackTrace();
            }
        }
    }
}
