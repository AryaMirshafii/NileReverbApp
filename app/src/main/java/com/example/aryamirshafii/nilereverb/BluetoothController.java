package com.example.aryamirshafii.nilereverb;

import android.annotation.SuppressLint;

import android.content.Context;
import android.util.Log;


import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.internal.operations.CharacteristicLongWriteOperation;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.polidea.rxandroidble2.scan.ScanFilter;

import java.nio.charset.StandardCharsets;

import java.util.UUID;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class BluetoothController {

    private RxBleClient rxBleClient;
    private Context context;

    private UUID uuid;
    private CommandController commandController;
    private String packetString;
    private String address = "C8:DF:84:2A:56:13";
    private final String deviceName = "NileReverb "; // Name has a space at the end due to weird  behavior from AT commands

    private RxBleDevice device;




    private RxBleConnection bleConnection;
    private io.reactivex.Observable<RxBleConnection> connectionObservable;
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private Disposable connectionDisposable;

    private String previousPacketString = "";

    private boolean deviceExists = false;


    private Disposable scanSubscription;


    public BluetoothController(Context appContext){
        this.context = appContext;
        this.rxBleClient = RxBleClient.create(appContext);

        uuid =  UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
        commandController = new CommandController(context);
        packetString = "";
        //device = rxBleClient.getBleDevice(address);
        scan();
        if(deviceExists){
            System.out.println("The device exists");
            scanAndConnect();

        } else{

            System.out.println("The device does not exist");
        }





    }

    private void scanAndConnect(){
        scanSubscription.dispose();
        System.out.println("Scanning and connecting");
        connectionObservable = prepareConnectionObservable();
        connect();
    }

    private void scan(){

        System.out.println("Starting to scan");




        scanSubscription= rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        //.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build()


        )
                .subscribe(
                        scanResult -> {

                            // System.out.println("THe device name is:" + scanResult.getBleDevice().getName()  + ":");
                            if(scanResult.getBleDevice().getName() != null && scanResult.getBleDevice().getName().equals(deviceName)){

                                device = scanResult.getBleDevice();
                                System.out.println("Found device!");
                                deviceExists = true;
                                scanAndConnect();


                            }


                        },
                        throwable -> {
                            System.out.println("An error occured while trying to scan for devices");
                            throwable.printStackTrace();
                        }
                );

        //scanSubscription.dispose();
    }



    private io.reactivex.Observable<RxBleConnection> prepareConnectionObservable() {
        return device
                .establishConnection(false);
    }


    private boolean isConnected() {
        return device.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }


    @SuppressLint("CheckResult")
    private void connect(){

        if (isConnected()) {
            triggerDisconnect();
        } else {
            connectionDisposable = device.establishConnection(false)

                    .doFinally(this::dispose)
                    .subscribe(this::onConnectionReceived, this::onConnectionFailure);
        }





    }

    @SuppressLint("CheckResult")
    public void read(){
        if (isConnected()) {


            bleConnection.setupNotification(uuid)
                    .doOnNext(notificationObservable -> {

                    })
                    .flatMap(notificationObservable -> notificationObservable) // <-- Notification has been set up, now observe value changes.
                    .subscribe(
                            bytes -> {
                                //System.out.println("Recieving data");
                                String encodedString = new String(bytes, StandardCharsets.UTF_8);
                                encodedString = trimString(encodedString);


                                if(!encodedString.equals("")){
                                    //System.out.println("Executing command.....");
                                    ensurePacket(encodedString);
                                }



                            },
                            throwable -> {
                                System.out.println("An error occured");
                                throwable.printStackTrace();

                            }
                    );

        }else {
            connect();
            System.out.println("I am not connected to deviec in reading");
        }



    }

    /**
     * Trims a string depending on the characters it contains
     * More specific then the normal trim function
     * @param string the string that needs to be trimmed
     * @return
     */
    public String trimString(String string){

        System.out.println("The trimmed string is:" + string + ":");
        string = string.trim();
        string = string.replace("\n", "");
        string = string.replace("*", " ");

        return string;
        /**
        int length = string.length();
        if(length  < 2){
            return string;
        }

        if(string.substring(1,2).equals("_") || string.substring(length -2, length -1).equals("_")){
            System.out.println("Contains _");
            string = string.trim();
            return string;
        }else if(string.substring(0,2).equals("  ") && string.substring(length -2, length).equals("  ")){
            System.out.println("Double spaces");
            string = string.substring(1, length -1);
            return string;
        }else if (string.substring(0,2).equals("  ")){
            System.out.println("One space at front");
            string = string.substring(1, length);
            return string;
        }else if(string.substring(length -2, length).equals("  ")){
            System.out.println("One space at end");
            string = string.substring(0, length -2 );
            return string;
        }

        System.out.println("No need to trim.");
        return string;
         */
    }




    @SuppressLint("CheckResult")
    public void write(String message){
        System.out.println("Writing " + message);
        if(message.equals("")){
            read();
            return;
        }

        if(message.charAt(0) != '_'){
            message = "_" + message;
        }

        if(message.charAt(message.length() -1) != '_'){
            message += '_';
        }
        byte[] byteArray = message.getBytes();
        System.out.println("ByteArray size is + " + byteArray.length);

        if (isConnected() && !connectionDisposable.isDisposed()) {


            bleConnection.createNewLongWriteBuilder()
                    .setCharacteristicUuid(uuid)

                    .setBytes(byteArray)
                    //.setMaxBatchSize(20) // optional -> default 20 or current MTU

                    .build()
                    .subscribe();


            read();

        }else {
            connect();
            System.out.println("Tried to write but could not connect");
        }
    }







    /**
     * A function that ensures that the data recieved from bluetooth is complete
     * I.E starts and ends with "_" in order to ensure a complete string of data.
     * @param commandString
     */
    private void ensurePacket(String commandString){

        System.out.println("command string is:" + commandString + ":");
        if(previousPacketString.equals(commandString)){
            System.out.println("Duplicate command strings detected. Returning");
            return;
        }





        previousPacketString = commandString;

        if(commandString.startsWith("_") && commandString.endsWith("_") && commandString.length() > 3){
            //Case 1 complete packet with front and end "_"
            System.out.println("Case 1");

            write(commandController.doCommand(commandString));

        }else if(commandString.startsWith("_") && !commandString.endsWith("_")){
            //Case 2 complete packet with front of "_"
            packetString = commandString;
            System.out.println("Case 2"+ packetString);
        }else if(commandString.endsWith("_") && !commandString.startsWith("_")){
            //Case 2 complete packet with end of "_"
            System.out.println("Case 3");
            packetString += commandString;
            packetString = packetString.replace("_","");
            write(commandController.doCommand(packetString));
            packetString = "";

        }else{
            System.out.println("Case 4");
            packetString += commandString;
        }

        System.out.println("Current packet is :" + packetString + ":");
    }





    private void onReadFailure(Throwable throwable) {
        //noinspection ConstantConditions
        System.out.println("An error occured while reading");
        System.out.println(throwable.getCause());
        throwable.printStackTrace();
    }


    private void onWriteFailure(Throwable throwable) {
        //noinspection ConstantConditions
        System.out.println("An error occured while writing");
        System.out.println(throwable.getCause());
        throwable.printStackTrace();
    }

    private void onWriteSuccess() {
        //noinspection ConstantConditions
        System.out.println("the write was sucessful");
    }

    private void onConnectionFailure(Throwable throwable) {
        //noinspection ConstantConditions
        System.out.println("An error occured while connecting");
        System.out.println(throwable.getCause());
        throwable.printStackTrace();
    }


    private void triggerDisconnect() {
        disconnectTriggerSubject.onNext(true);
    }


    private void dispose() {
        connectionDisposable = null;

    }


    private void onConnectionReceived(RxBleConnection connection) {

        System.out.println("A connection has occurred");
        bleConnection = connection;
        read();

    }





}