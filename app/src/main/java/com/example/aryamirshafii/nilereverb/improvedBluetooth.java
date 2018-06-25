package com.example.aryamirshafii.nilereverb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;


import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import java.nio.charset.StandardCharsets;

import java.util.UUID;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class improvedBluetooth {

    private RxBleClient rxBleClient;
    private Context context;

    private UUID uuid;
    private CommandController commandController;
    private String packetString;
    private String address = "C8:DF:84:2A:56:13";
    private RxBleDevice device;




    private RxBleConnection bleConnection;
    private boolean isConnected = false;
    private io.reactivex.Observable<RxBleConnection> connectionObservable;
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private String currentCommand ="";
    private Disposable connectionDisposable;
    public improvedBluetooth(Context appContext){
        this.context = appContext;
        this.rxBleClient = RxBleClient.create(appContext);

        uuid =  UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
        commandController = new CommandController(context);
        packetString = "";
        device = rxBleClient.getBleDevice(address);
        connectionObservable = prepareConnectionObservable();
        connect();




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
                                System.out.println("Recieving data");
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

        }else {
            System.out.println("I am not connected to deviec in reading");
        }



    }




    @SuppressLint("CheckResult")
    public void write(String message){
        System.out.println("Writing " + message);

        if(message.charAt(0) != '_'){
            message = "_" + message;
        }

        if(message.charAt(message.length() -1) != '_'){
            message += '_';
        }
        byte[] byteArray = message.getBytes();

        if (isConnected()) {

            bleConnection.createNewLongWriteBuilder()
                    .setCharacteristicUuid(uuid)

                    .setBytes(byteArray)

                    .build()
                    .subscribe();


        read();

        }else {
            System.out.println("Tried to write but could not connect");
        }
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
