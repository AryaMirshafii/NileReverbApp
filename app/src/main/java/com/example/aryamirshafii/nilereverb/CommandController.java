package com.example.aryamirshafii.nilereverb;

import android.content.ContentResolver;
import android.content.Context;

import java.util.ArrayList;

public class CommandController {

    private Musicmanager songManager;
    private String nowPlayingSongName;
    private String currentCommand = "";



    public CommandController(Context context){
        songManager = new Musicmanager(context.getContentResolver(),context);
        songManager.prepare();

    }

    /**
     * A simple function to execute commands
     * always used when the nile reverb sends a command to the phone
     * @param command the command to execute
     */
    public String doCommand(String command){
        //System.out.println("Current Command is :" + currentCommand +" And the new one is" + command);
        if(currentCommand.trim().equals(command.trim())){
            System.out.println("Repeat Command Detected");
            return "Redundant Command";
        }
        currentCommand = command;
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


//        if(command.equals("play Hotel California") && nowPlayingSongName == null){
//            songManager.playHotelCalifornia();
//            nowPlayingSongName = "Hotel California";
//            System.out.println("Playing " + nowPlayingSongName);
//        }


        return "Invalid request";
    }



}
