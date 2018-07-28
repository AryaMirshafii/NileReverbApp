package com.example.aryamirshafii.nilereverb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
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
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class weatherManager implements LocationListener {

    final Context context;
    private Location theLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private dataController DataManager;
    private String locationString;
    private Geocoder geocoder;


    @SuppressLint("CheckResult")
    public weatherManager(Context aContext) {
        this.context = aContext;


        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.theLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        this.isLocationEnabled();

        //bluetoothController = new BluetoothController(context);
        DataManager = new dataController(context);
        getWeather();
        Completable.timer(6, TimeUnit.SECONDS, Schedulers.computation())
                .subscribe(() -> {
                    System.out.println("The location string is found to be :" + locationString);

                });


    }
    public String getLocationString(){
        return locationString;
    }


    /**
     * A method that check if location is enabled
     * If location is not enabled it will prompt the user to do so
     */
    private void isLocationEnabled() {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();

            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alert.show();
        }

    }

    public String getWeather(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }

        if(theLocation == null){
            System.out.println("Location is null");
            theLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        }
        Double latitude = theLocation.getLatitude();
        Double longitude = theLocation.getLongitude();

        geocoder =  new Geocoder(context, Locale.getDefault());

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {

            e.printStackTrace();
        }

        if (addresses != null) {

            String city = addresses.get(0).getLocality();
            locationString = city;
            System.out.println("The city is: " + city );

            String keyRequest = "&appid=e078b8fc5e1bafdfc2758d78ae96b10b";
            String requestString = "http://api.openweathermap.org/data/2.5/weather?q=" + city.trim() + keyRequest;
            return requestString;
        }else {
            System.out.println("The city addresses is null ");
        }

        return null;

    }




    public String getWeather(String location){

        location = location.substring(0, 1).toUpperCase() + location.substring(1);
        String keyRequest = "&appid=e078b8fc5e1bafdfc2758d78ae96b10b";
        String requestString = "http://api.openweathermap.org/data/2.5/weather?q=" +location.trim() + keyRequest;
        requestString = requestString.trim();
        locationString = location.trim();
        return requestString;



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
