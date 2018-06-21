package com.example.aryamirshafii.nilereverb;

import android.content.ContentResolver;
import android.content.Context;

import java.util.ArrayList;

public class CommandController {

    private Musicmanager songManager;
    private String nowPlayingSongName;



    public CommandController(Context context){
        songManager = new Musicmanager(context.getContentResolver(),context);
        songManager.prepare();

    }

    /**
     * A simple function to execute commands
     * always used when the nile reverb sends a command to the phone
     * @param command the command to execute
     */
    public void doCommand(String command){
        command = command.trim().toLowerCase();
        System.out.println("doing command  " + command);
        if(command.contains("play")){
            if(command.contains(" some ") && command.contains("play")){
                command = command.replace("play","");
                command = command.replace("some","");
                songManager.playArtist(command.trim());

            }else if(nowPlayingSongName == null || !command.contains(nowPlayingSongName.toLowerCase().trim())) {
                command = command.replace("play","");
                nowPlayingSongName = command;
                songManager.playSong(command);
                System.out.println("Playing " + nowPlayingSongName);
            }
        }


//        if(command.equals("play Hotel California") && nowPlayingSongName == null){
//            songManager.playHotelCalifornia();
//            nowPlayingSongName = "Hotel California";
//            System.out.println("Playing " + nowPlayingSongName);
//        }
    }



}
