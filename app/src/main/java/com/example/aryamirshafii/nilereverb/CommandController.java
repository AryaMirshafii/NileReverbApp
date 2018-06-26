package com.example.aryamirshafii.nilereverb;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;

public class CommandController {

    private Musicmanager songManager;
    private String nowPlayingSongName;
    private String currentCommand = "";
    private weatherManager weatherController;
    private dataController dataManager;



    public CommandController(Context context){
        songManager = new Musicmanager(context.getContentResolver(),context);
        songManager.prepare();
        weatherController = new weatherManager(context);
        dataManager = new dataController(context);
        //weatherController.getWeather();

    }

    /**
     * A simple function to execute commands
     * always used when the nile reverb sends a command to the phone
     * @param command the command to execute
     */
    public String doCommand(String command){
        //System.out.println("Current Command is :" + currentCommand +" And the new one is" + command);

        command = command.trim().toLowerCase();
        System.out.println("doing command  " + command);
        if(command.contains("play")){
            if(command.contains(" some ") && command.contains("play")){
                command = command.replace("play","");
                command = command.replace("some","");
                return songManager.playArtist(command.trim());

            }else if(nowPlayingSongName == null || !command.contains(nowPlayingSongName.toLowerCase().trim())) {
                command = command.replace("play","");
                nowPlayingSongName = command;
                System.out.println("Playing " + nowPlayingSongName);
                return songManager.playSong(command);

            }
        }

        if(command.contains("weather")){
            weatherController.getWeather();
            try {
                //set time in mili
                Thread.sleep(3000);

            }catch (Exception e){
                e.printStackTrace();
            }


            return "UpdateW" + dataManager.getWeather();
        }



//        if(command.equals("play Hotel California") && nowPlayingSongName == null){
//            songManager.playHotelCalifornia();
//            nowPlayingSongName = "Hotel California";
//            System.out.println("Playing " + nowPlayingSongName);
//        }


        return "Invalid request";
    }



}
