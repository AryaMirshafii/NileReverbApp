package com.example.aryamirshafii.nilereverb;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
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
    private CommandController commandController;
    private String packetString;
    private Disposable connectionDisposable;
    private String address = "C8:DF:84:2A:56:13";
    private String currentCommand ="";

    public BluetoothController(Context appContext){
        this.context = appContext;
        this.rxBleClient = RxBleClient.create(appContext);

        uuid =  UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
        //uuid =  UUID.fromString("FFE1");
        commandController = new CommandController(context);
        //checkBluetooth();

        packetString = "";
        read();


    }




    private void checkBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        }
    }




    @SuppressLint("CheckResult")
    public void read(){
        System.out.println("Starting Connection");


        RxBleDevice device = rxBleClient.getBleDevice(address);

        connectionDisposable =  device.establishConnection(false)
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(uuid))
                .doOnNext(notificationObservable -> {

                })
                .flatMap(notificationObservable -> notificationObservable) // <-- Notification has been set up, now observe value changes.
                .subscribe(
                        bytes -> {

                            String encodedString = new String(bytes, StandardCharsets.UTF_8);
                            encodedString = encodedString.trim();
                            if(!encodedString.equals("")){
                                System.out.println("Executing command.....");
                                ensurePacket(encodedString);
                            }



                        },
                        throwable -> {
                            System.out.println("An error occured");
                            throwable.printStackTrace();

                        }
                );
    }

    /**
     * A function that writes values to the bluetooth module
     * @param message the message to be sent to the bluetooth
     */
    @SuppressLint("CheckResult")
    public void write(String message){



        if(message.charAt(0) != '_'){
            message = "_" + message;
        }

        if(message.charAt(message.length() -1) != '_'){
            message += '_';
        }

        byte[] byteArray = message.getBytes();


        RxBleDevice device = rxBleClient.getBleDevice(address);

        if(connectionDisposable != null){
            System.out.println("Disposing disposable");
            //connectionDisposable.dispose();
        }

        connectionDisposable = device.establishConnection(false)
                .flatMap(rxBleConnection -> rxBleConnection.createNewLongWriteBuilder()
                        .setCharacteristicUuid(uuid)

                        .setBytes(byteArray)

                        .build()
                )
                .subscribe(
                        bytes -> {
                            // Written data.
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            System.out.println("Wrote unsuccessfully");
                        }
                );

        connectionDisposable.dispose();
        read();
    }


    /**
     * A function that ensures that the data recieved from bluetooth is complete
     * I.E starts and ends with "_" in order to ensure a complete string of data.
     * @param commandString
     */
    private void ensurePacket(String commandString){
        if(currentCommand.trim().equals(commandString.trim())){
            Log.d("Ensuring Packet", "returning from packet");
            System.out.println("returning from packet");
            return;
        }
        System.out.println("command string is" + commandString);

        if(commandString.startsWith("_") && commandString.endsWith("_") && commandString.length() > 3){
            //Case 1 complete packet with front and end "_"
            System.out.println("Case 1");

            if(!currentCommand.trim().equals(commandString.trim())){
                currentCommand = commandString.trim();
                write(commandController.doCommand(commandString));
            }

        }else if(commandString.startsWith("_")  && packetString.length() == 0){
            //Case 2 complete packet with front of "_"
            packetString = commandString;
            System.out.println("Case 2  "+ packetString);
        }else if(commandString.endsWith("_")){
            //Case 2 complete packet with end of "_"
            System.out.println("Case 3");
            packetString += commandString;
            packetString = packetString.replace("_","");
            if(!currentCommand.equals(commandString)){
                currentCommand = commandString.trim();
                write(commandController.doCommand(packetString));
            }
            packetString = "";

        }else{
            System.out.println("Case 4");
            packetString += commandString;
        }
    }




}