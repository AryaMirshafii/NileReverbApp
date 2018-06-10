package com.example.aryamirshafii.nilereverb;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.widget.TextView;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import io.reactivex.disposables.Disposable;

public class BluetoothController {
    private RxBleClient rxBleClient;
    private Context context;
    private TextView label;
    private UUID uuid;

    public BluetoothController(Context appContext, TextView bluetoothLabel){
        this.context = appContext;
        this.rxBleClient = RxBleClient.create(appContext);
        this.label = bluetoothLabel;
        uuid =  UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
        //uuid =  UUID.fromString("FFE1");
        checkBluetooth();



    }


    private void checkBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        }
    }

    public void connect(){
        //"AA:BB:CC:DD:EE:FF";
        String address = "C8:DF:84:2A:56:13";
        RxBleDevice device = rxBleClient.getBleDevice(address);


        Disposable disposable = device.establishConnection(false) // <-- autoConnect flag
                .subscribe(
                        rxBleConnection -> {
                            // All GATT operations are done through the rxBleConnection.
                            System.out.println("I am connected to " + device.getBluetoothDevice().getUuids());
                            label.setText("Connected to Nile Reverb");
                            //read();
                        },
                        throwable -> {
                            // Handle an error here.
                            System.out.println("I am not connected");
                        }
                );

        // When done... dispose and forget about connection teardown :)
        //disposable.dispose();
    }


    @SuppressLint("CheckResult")
    public void read(){

        String address = "C8:DF:84:2A:56:13";

        RxBleDevice device = rxBleClient.getBleDevice(address);
        device.establishConnection(false)
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(uuid))
                .doOnNext(notificationObservable -> {
                    // Notification has been set up
                })
                .flatMap(notificationObservable -> notificationObservable) // <-- Notification has been set up, now observe value changes.
                .subscribe(
                        bytes -> {

                            String encodedString = new String(bytes, StandardCharsets.UTF_8);
                            System.out.println("BYTes is" + encodedString);
                        },
                        throwable -> {
                            throwable.printStackTrace();
                        }
                );
    }




}
