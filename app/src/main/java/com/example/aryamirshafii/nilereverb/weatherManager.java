package com.example.aryamirshafii.nilereverb;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class weatherManager implements LocationListener{

    final Context context;
    private Location theLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private dataController DataManager;



    public weatherManager(Context aContext){
        this.context = aContext;




        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.theLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        this.isLocationEnabled();

        //bluetoothController = new BluetoothController(context);
        DataManager = new dataController(context);
        getWeather();


    }


    /**
     * A method that check if location is enabled
     * If location is not enabled it will prompt the user to do so
     */
    private void isLocationEnabled() {

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            AlertDialog alert= alertDialog.create();

            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alert.show();
        }

    }





    public void getWeather(){
        theLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(theLocation == null){
            System.out.println("Location is null");
            theLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return;
        }



        //Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        System.out.println("The location is " + theLocation);

        getCityName(theLocation, new OnGeocoderFinishedListener() {
            @Override
            public void onFinished(List<Address> results) {
                if(results.size() < 0){
                    return;
                }
                Address address = results.get(0);
                System.out.println(address.getLocality());
                //key = e078b8fc5e1bafdfc2758d78ae96b10b
                String keyRequest = "&appid=e078b8fc5e1bafdfc2758d78ae96b10b";
                String requestString = "http://api.openweathermap.org/data/2.5/weather?q=" + address.getLocality().trim() + keyRequest;
                requestString = requestString.trim();
                try {
                    getDataFromURL(requestString, address.getLocality().trim());
                    System.out.println("THe data is");
                    //System.out.println(data);
                } catch (IOException e) {
                    System.out.println("A IOException occured");
                    e.printStackTrace();
                } catch (JSONException e) {
                    System.out.println("A JSONException occured");
                    e.printStackTrace();
                }


            }
        });


    }

    public void getDataFromURL(String urlString, String address) throws IOException, JSONException {

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






                            //weatherManager.this.bluetoothController.write("_UpdateW" + Fahrenheit +"_");
                            DataManager.setWeather(
                                    Integer.toString(finalTemp.intValue())
                                    + "," + address
                            );



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








    @SuppressLint("StaticFieldLeak")
    private void getCityName(final Location location, final OnGeocoderFinishedListener listener) {
        new AsyncTask<Void, Integer, List<Address>>() {
            @Override
            protected List<Address> doInBackground(Void... arg0) {
                Geocoder coder = new Geocoder(context, Locale.ENGLISH);
                List<Address> results = null;
                try {
                    results = coder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    // nothing
                }
                return results;
            }

            @Override
            protected void onPostExecute(List<Address> results) {
                if (results != null && listener != null) {
                    listener.onFinished(results);
                }
            }
        }.execute();
    }

    @Override
    public void onLocationChanged(Location location) {
        theLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
