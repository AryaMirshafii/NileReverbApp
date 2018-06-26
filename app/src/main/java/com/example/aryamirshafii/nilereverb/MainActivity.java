package com.example.aryamirshafii.nilereverb;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private TextView bluetoothStatusLabel;
    private BluetoothDevice hearingBluetooth;
    private BluetoothAdapter myBluetooth;
    private Musicmanager songManager;
    private boolean isRegistered = false;
    private BluetoothController bluetoothController;

    private weatherManager weatherController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songManager = new Musicmanager(getContentResolver(),getApplicationContext());
        songManager.prepare();
        setContentView(R.layout.activity_main);
        bluetoothStatusLabel = findViewById(R.id.bluetoothLabel);

        weatherController = new weatherManager(getApplicationContext());


        Button closingButton = (Button) findViewById(R.id.playButton);
        bluetoothController = new BluetoothController(getApplicationContext());
        // Set a click listener for the popup window close button
        closingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Button Pressed");
                //weatherController.getWeather();
                //bluetoothController.write("Hi arya");
                //bluetoothController.read();
                bluetoothController.write("_Arya is so handsome_");
            }
        });


        //UNcomment these
        //bluetoothController.connect();



    }


    private void testSongmanager(){
        songManager.playSong("Jumpin Jack Flash");
        songManager.playArtist("foreigner");
        songManager.printTest("Sgt. Pepper's Lonely Hearts Club Band");
    }


}
