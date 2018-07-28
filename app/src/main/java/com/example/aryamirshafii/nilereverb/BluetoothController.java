package com.example.aryamirshafii.nilereverb;

import android.annotation.SuppressLint;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.internal.operations.CharacteristicLongWriteOperation;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.polidea.rxandroidble2.scan.ScanFilter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import pl.droidsonroids.gif.GifDrawable;

public class BluetoothController {

    private RxBleClient rxBleClient;
    private Context context;

    private UUID uuid;

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

    private GifDrawable gifDrawable;
    private TextView bluetoothLabel;

    private PopupWindow popupWindow;

    private BluetoothDevice audioBluetooth;
    private UUID audioUUID = UUID.fromString("0000110A-0000-1000-8000-00805F9B34FB");

    private RxBleConnection.LongWriteOperationBuilder longWriteConnection;









    //The variables from the original CommandController class
    private Musicmanager songManager;
    private String nowPlayingSongName;
    private String currentCommand = "";
    private weatherManager weatherController;
    private dataController dataManager;
    private PhoneController phoneController;
    private StringController stringController;



    public BluetoothController(Context appContext, GifDrawable drawable, TextView btLabel){
        this.context = appContext;
        this.rxBleClient = RxBleClient.create(appContext);
        this.gifDrawable = drawable;
        this.bluetoothLabel = btLabel;




        songManager = new Musicmanager(context.getContentResolver(),context);
        songManager.prepare();
        weatherController = new weatherManager(context);
        weatherController.getWeather();
        dataManager = new dataController(context);
        //weatherController.getWeather();
        phoneController = new PhoneController(context);
        stringController = new StringController();





        gifDrawable.pause();
        uuid =  UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");

        packetString = "";
        //device = rxBleClient.getBleDevice(address);
        scan();
        if(deviceExists){
            System.out.println("The device exists");
            scanAndConnect();

        } else{

            System.out.println("The device does not exist");
        }

        if(!checkBothDevices()){
            System.out.println("The BT SPeaker is not connected");
            String popupMessage = "Please pair your device with the Nile Reverb's Speaker";
            showPopup(popupMessage);
        }else {
            System.out.println("The BT SPeaker is connected");
            System.out.println("Create bond is " + audioBluetooth.createBond());



        }




    }






    private boolean checkBothDevices(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        //List<String> bluetoothList = new ArrayList<>();
        for(BluetoothDevice btDevice : pairedDevices){

            if(btDevice.getName().equals("BT Speaker")){
                audioBluetooth = btDevice;
                return true;
            }

        }

        return false;

    }

    private void showPopup(String popupText){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.music_pair_warning,null);
        TextView textView = customView.findViewById(R.id.warningText);
        textView.setText(popupText);
        popupWindow = new PopupWindow(
                customView,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        // Set an elevation value for popup window
        // Call requires API level 21
        if(Build.VERSION.SDK_INT>=21){
            popupWindow.setElevation(5.0f);
        }

        Button closingButton = (Button) customView.findViewById(R.id.closePopup);

        // Set a click listener for the popup window close button
        closingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                popupWindow.dismiss();
            }
        });


        //popupWindow.showAtLocation(null, Gravity.CENTER,0,0);
    }

    private void scanAndConnect(){
        scanSubscription.dispose();
        System.out.println("Scanning and connecting");
        connectionObservable = prepareConnectionObservable();
        connect();
    }

    private void scan(){

        System.out.println("Starting to scan");




        scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        //.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build()


        )
                .subscribe(
                        scanResult -> {

                            //System.out.println("THe device name is:" + scanResult.getBleDevice().getName()  + ":");
                            if(scanResult.getBleDevice().getName() != null && scanResult.getBleDevice().getName().equals(deviceName)){

                                device = scanResult.getBleDevice();
                                System.out.println("Found device!");
                                bluetoothLabel.setText("Connected To Device");
                                deviceExists = true;
                                scanAndConnect();


                            }else {
                                bluetoothLabel.setText("Device Not Connected");
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

        //longWriteConnection =
        //        bleConnection.createNewLongWriteBuilder()
        //                .setCharacteristicUuid(uuid);





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
                                    gifDrawable.start();
                                }else {
                                    gifDrawable.pause();
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
        //string = string.replace("\n", "");
        string = string.replaceAll("[^\\x00-\\x7F]", "");
        string = string.replaceAll("[\u0000-\u001f]", "");
        string = string.trim();
        string = string.replace("*", " ");
        System.out.println("The trimmed string is1:" + string + ":");


        /**
        //string = string.trim();
        if(string.length() > 3 && string.charAt(string.length() -2)== '_'){
            System.out.println("Created substring");
            string = string.substring(0,string.length() -1);
        }
         */
        //string = string.replace("\n", "");


        return string;


    }




    @SuppressLint("CheckResult")
    public void write(String message){
        message = message.replace("\n","");
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
            if(longWriteConnection == null){

                longWriteConnection =
                       bleConnection.createNewLongWriteBuilder()
                               .setCharacteristicUuid(uuid);
            }

            longWriteConnection
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

        if(packetString != null && packetString.length() > 4 && packetString.indexOf("_", 1) > 0){
            System.out.println("Duplicate underscores detected");
            int secondUnderscoreIndex = packetString.indexOf("_", 1);
            packetString = packetString.substring(0, secondUnderscoreIndex + 1);


        }




        previousPacketString = commandString;

        if(commandString.startsWith("_") && commandString.endsWith("_") && commandString.length() > 3){
            //Case 1 complete packet with front and end "_"
            System.out.println("Case 1");

            write(doCommand(commandString));

        }else if(commandString.startsWith("_") && !commandString.endsWith("_")){
            //Case 2 complete packet with front of "_"
            packetString = commandString;
            System.out.println("Case 2"+ packetString);
        }else if(commandString.endsWith("_") && !commandString.startsWith("_")){
            //Case 2 complete packet with end of "_"
            System.out.println("Case 3");
            packetString += commandString;
            packetString = packetString.replace("_","");
            write(doCommand(packetString));

            packetString = "";

        }else{
            System.out.println("Case 4");
            packetString += commandString;
            if(isValidCommand(packetString)){
                write(doCommand(packetString));
            }
        }

        System.out.println("Current packet is :" + packetString + ":");
    }

    private void trimEnd(String toTrim){

    }
    private boolean isValidCommand(String command){
        if(command.length() < 3){
            return false;
        }

        System.out.println("The char at index 1 is :" + command.charAt(0));
        System.out.println("The char at end  is :" + command.charAt(command.length() -1));
        return command.startsWith("_") && command.endsWith("_");
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
        //System.out.println(throwable.));
        bluetoothLabel.setText("Device Not Connected");
        throwable.printStackTrace();
        connect();
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
        gifDrawable.start();

    }









    /**
     * A simple function to execute commands
     * always used when the nile reverb sends a command to the phone
     * @param command the command to execute
     */
    @SuppressLint("CheckResult")
    public String doCommand(String command){

        System.out.println("Current Command is :" + command);
        command = command.trim();



        command = command.trim().toLowerCase();
        if(currentCommand.equals(command)){
            System.out.println("The previous command is  " + currentCommand);
            System.out.println("The current command is  " + command);
            System.out.println("Duplicate command detected");
            //return "Duplicate command detected"; // In case of a duplicate command return ""
            return "";
        }


        System.out.println("doing command" + command);
        currentCommand = command;



        if (command.contains("text ")) {
            command = command.replace("text", "");
            command = command.replace("_", "");
            command = command.trim();



            Completable.timer(4, TimeUnit.SECONDS, Schedulers.computation())
                    .subscribe(() -> {
                        System.out.println("2 Seconds have elapsed. Resetting current command");
                        currentCommand = "";
                    });

            if(command.contains("message")){

                System.out.println("Single value passed into text");
                command = command.replace("message", "");
                command = command.trim();
                return phoneController.text(command);
            }

            if(countSpaces(command) > 1){
                command = command.replaceFirst(" ", ",");
                System.out.println("The replacement command is" + command);
                String[] splitCommand = command.split(",");

                return phoneController.text(splitCommand[0],splitCommand[1]);

            } else {

                command = command.replace("text", "");
                command = command.trim();
                if(phoneController.checkContact(command)){
                    phoneController.setCurrentContact(command);
                    return "Ask what would you like to text " + command + "?";
                }else {
                    return "This contact does not exist";
                }


            }





        }else if(command.contains("play next")){
            //Accounting for back to back play next requests. Sometimes the bluetooth module
            //sends duplicate data so the app will make sure that next or previous requests are
            //executed at least 2 seconds apart.
            Completable.timer(1, TimeUnit.SECONDS, Schedulers.computation())
                    .subscribe(() -> {
                        System.out.println("1 Seconds have elapsed");
                        currentCommand = "";
                    });
            System.out.println("Playing next song");
            return songManager.playNext();
        } else if(command.contains("play previous")) {

            //Accounting for back to back play next requests. Sometimes the bluetooth module
            //sends duplicate data so the app will make sure that next or previous requests are
            //executed at least 2 secinds apart.
            Completable.timer(1, TimeUnit.SECONDS, Schedulers.computation())
                    .subscribe(() -> {
                        System.out.println("1 Seconds have elapsed");
                        currentCommand = "";
                    });

            System.out.println("Playing previous song");
            return songManager.playPrevious();
        }
        if(command.contains("play ")){
            if(command.contains(" some ") && command.contains("play ")){
                command = command.replace("play","");
                command = command.replace("some","");
                return songManager.playArtist(command.trim());

            }else if(nowPlayingSongName == null || !command.contains(nowPlayingSongName.toLowerCase().trim())) {
                command = command.replace("play","");
                nowPlayingSongName = command;
                System.out.println("Playing " + nowPlayingSongName);
                return songManager.playSong(command);

            }



        }else if(command.contains("go")){
            if(command.contains("next") || command.contains("forward")){

                //Accounting for back to back play next requests. Sometimes the bluetooth module
                //sends duplicate data so the app will make sure that next or previous requests are
                //executed at least 2 seconds apart.
                Completable.timer(4, TimeUnit.SECONDS, Schedulers.computation())
                        .subscribe(() -> {
                            System.out.println("2 Seconds have elapsed");
                            currentCommand = "";
                        });

                return songManager.playNext();
            } else if(command.contains("previous") || command.contains("back")){



                return songManager.playPrevious();
            }


        }else if(command.contains("pause")){



            return songManager.pause();

        } else if(command.contains("start") || command.contains("continue")){


            return songManager.startPlaying();
        } else if(command.contains("weather")){

            if(command.contains(" in ")){

                String locality = command.substring(command.lastIndexOf(" in ") + 4);
                locality = locality.replace("_","");
                System.out.println("Getting weather for " + locality);


                try {
                    locality = stringController.capitalize(locality);
                    getDataFromURL(weatherController.getWeather(locality), locality);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }



                return "";
                //return "UpdateW" + dataManager.getWeather();

            }else {
                System.out.println("Getting weather");


                try {
                    getDataFromURL(weatherController.getWeather(), weatherController.getLocationString());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //System.out.println("Get weather is :);
                return "";
            }

        } else if(command.contains("pause") || command.contains("stop")){
            return songManager.pause();
        }else if(command.contains("start") || command.contains("resume")){
            return songManager.startPlaying();
        }


        return "Invalid request";
    }

    private int countSpaces(String toAnalyze){
        int count =0;

        for(int i=0;i<toAnalyze.length();i++){
            if(Character.isWhitespace(toAnalyze.charAt(i))){
                count+=1;
            }
        }

        return count;
    }

    private void getDataFromURL(String urlString, String address) throws IOException, JSONException {
        System.out.println("The entered address is :" + address + ":");

        AndroidNetworking.get(urlString)
                .addPathParameter("pageNumber", "0")
                .addQueryParameter("limit", "3")
                .addHeaders("token", "1234")
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("The data is " + response);
                        try {
                            System.out.println("The data is " + response.get("main").toString());
                            String first = Arrays.asList(response.get("main").toString()
                                    .split(","))
                                    .get(0);
                            System.out.println("THe first string is " + first);
                            first = first.replace("{" + "\"" + "temp" + "\"" + ":","");
                            System.out.println("THe trimmed string is " + first);


                            Double finalTemp =  ((Double.parseDouble(first) *  9/5) - 459.6700);



                            System.out.println("THe temp  is " + finalTemp);
                            System.out.println("The address for the weather is" + address);

                            write("UpdateW" + Integer.toString(finalTemp.intValue()) + "," + address);







                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        System.out.println("An error occured");
                        anError.printStackTrace();
                    }
                });
    }








}