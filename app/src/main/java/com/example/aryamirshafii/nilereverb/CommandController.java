package com.example.aryamirshafii.nilereverb;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class CommandController {

    private Musicmanager songManager;
    private String nowPlayingSongName;
    private String currentCommand = "";
    private weatherManager weatherController;
    private dataController dataManager;
    private PhoneController phoneController;


    public CommandController(Context context){
        songManager = new Musicmanager(context.getContentResolver(),context);
        songManager.prepare();
        weatherController = new weatherManager(context);
        dataManager = new dataController(context);
        //weatherController.getWeather();
        phoneController = new PhoneController(context);

    }

    /**
     * A simple function to execute commands
     * always used when the nile reverb sends a command to the phone
     * @param command the command to execute
     */
    @SuppressLint("CheckResult")
    public String doCommand(String command){

        System.out.println("Current Command is :" + command);




        command = command.trim().toLowerCase();
        if(currentCommand.equals(command)){
            System.out.println("Duplicate command detected");
            return "Duplicate command detected"; // In case of a duplicate command return ""
        }


        System.out.println("doing command" + command);
        currentCommand = command;
            if(command.contains("play next")){
                //Accounting for back to back play next requests. Sometimes the bluetooth module
                //sends duplicate data so the app will make sure that next or previous requests are
                //executed at least 2 seconds apart.
                Completable.timer(4, TimeUnit.SECONDS, Schedulers.computation())
                    .subscribe(() -> {
                        System.out.println("2 Seconds have elapsed");
                        currentCommand = "";
                    });
            System.out.println("Playing next song");
            return songManager.playNext();
        } else if(command.contains("play previous")) {

                //Accounting for back to back play next requests. Sometimes the bluetooth module
                //sends duplicate data so the app will make sure that next or previous requests are
                //executed at least 2 secinds apart.
                Completable.timer(4, TimeUnit.SECONDS, Schedulers.computation())
                        .subscribe(() -> {
                            System.out.println("2 Seconds have elapsed");
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



        }else if(command.contains(" go ")){
            if(command.contains(" next ") || command.contains(" forward ")){

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
            weatherController.getWeather();
            try {
                //set time in mili
                Thread.sleep(3000);
                
            }catch (Exception e){
                e.printStackTrace();
            }


            return "UpdateW" + dataManager.getWeather();
        } else if (command.contains("text ")) {
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



}
