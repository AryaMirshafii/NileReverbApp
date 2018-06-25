package com.example.aryamirshafii.nilereverb;

import android.content.Context;
import android.text.Editable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by aryamirshafii on 1/14/18.
 */

public class dataController {
    private Context context;

    private String shuffleStateFile = "shuffleState.txt";
    private String weatherFile = "weather.txt";

    public dataController(Context context){
        this.context = context;

    }


    /**
     * A function that saves the shuffle state to a text file
     * @param shuffleState the shuffle state either true or false
     */

    public void setShuffleState(Boolean shuffleState){
        String shuffle = shuffleState.toString();
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(shuffleStateFile , Context.MODE_PRIVATE);
            outputStream.write(shuffle.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * returns the shuffle state
     * @return
     */
    public Boolean getShuffleState(){
        FileInputStream fis;
        int n;
        try {
            fis = context.openFileInput(shuffleStateFile);
            StringBuffer fileContent = new StringBuffer("");

            byte[] buffer = new byte[1024];



            while ((n = fis.read(buffer)) != -1)
            {
                fileContent.append(new String(buffer, 0, n));
            }

            return Boolean.valueOf(fileContent.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }



    public void setWeather(String weather){

        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(weatherFile , Context.MODE_PRIVATE);
            outputStream.write(weather.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    public String getWeather(){
        FileInputStream fis;
        int n;
        try {
            fis = context.openFileInput(weatherFile);
            StringBuffer fileContent = new StringBuffer("");

            byte[] buffer = new byte[1024];



            while ((n = fis.read(buffer)) != -1)
            {
                fileContent.append(new String(buffer, 0, n));
            }

            return fileContent.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";

    }





}
