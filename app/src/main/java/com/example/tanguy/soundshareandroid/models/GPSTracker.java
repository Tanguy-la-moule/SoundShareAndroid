package com.example.tanguy.soundshareandroid.models;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


@SuppressLint("Registered")
public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;
    //public static final Locale fr_FR;

    //GPS, network and localisation status
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;
    double altitude;
    float accuracy;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    /**
     * Check if localisation permission is granted
     * @return boolean
     */
    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = mContext.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Get current location
     * @return Location
     */
    public Location getLocation() {
        startUsingGPS();
        stopUsingGPS();
        return location;
    }

    /**
     * Stop the GPSTracker
     */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Start the GPSTracker
     */
    public void startUsingGPS(){
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no GPS nor network
            } else {
                this.canGetLocation = true;

                if (isNetworkEnabled) {
                    if (!checkLocationPermission()) {
                        ActivityCompat.requestPermissions((Activity)mContext, new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                        }, 10);
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, MIN_TIME_BW_UPDATES, this);
                    Log.d("Network", "Network");

                    if (locationManager != null) {
                        if (!checkLocationPermission()) {
                            ActivityCompat.requestPermissions((Activity)mContext, new String[]{
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                            }, 10);
                        }
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            altitude = location.getAltitude();
                        }
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                altitude = location.getAltitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get current latitude if locatable
     * @return double
     */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    /**
     * Get current longitude if locatable
     * @return double
     */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    /**
     * Get current altitude if locatable
     * @return double
     */
    public double getAltitude(){
        if(location != null){
            altitude = location.getAltitude();
        }
        return altitude;
    }

    /**
     * Get current accuracy if locatable
     * @return double
     */
    public float getAccuracy(){
        if(location != null){
            accuracy = location.getAccuracy();
        }
        return accuracy;
    }

    /**
     * Check if device can be localised
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Show phone's localisation, network and permissions settings
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Alert title
        alertDialog.setTitle("Paramètres GPS");
        // Alert message
        alertDialog.setMessage("Voulez-vous accéder aux options GPS ?");

        // Positive button : show options
        alertDialog.setPositiveButton("Options", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        // Negative button
        alertDialog.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    /**
     * Get literal address from your location
     * @return String
     */
    public String getAdress(){

        String StrAdresse=null;
        List<Address> addresses = null;
        Geocoder geo = new Geocoder(this.mContext, Locale.getDefault());

        try {
            addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address adr=addresses.get(0);
        StrAdresse=adr.getAddressLine(0);
        return StrAdresse;
    }

    /**
     * Get distance (in km) between your position and the target position
     * @param LatTarget latitude of target position
     * @param LongTarget longitude of target position
     * @return float
     */
    public float getDistance(double LatTarget, double LongTarget) {
        Location locationA = new Location("point A");
        locationA.setLatitude(location.getLatitude());
        locationA.setLongitude(location.getLongitude());

        Location locationB = new Location("point B");
        locationB.setLatitude(LatTarget);
        locationB.setLongitude(LongTarget);

        return locationA.distanceTo(locationB);
    }

    /**
     * Get number of satellites
     * @return int
     */
    //TODO
    // Renvoie le nombre de satellites
    public int getSat() {
        int res=0;
        return res;
    }

    /**
     * Get latitude of target address
     * @param searchedAddress literal expression of target address
     * @return double
     */
    public double getLatitudeFromAddress(String searchedAddress){

        double LatAdresse=0;
        List<Address> addresses = null;
        Geocoder geo = new Geocoder(this.mContext, Locale.getDefault());
        try {
            addresses = geo.getFromLocationName(searchedAddress,1);
            if(addresses!=null)
            {
                Address returnedAddress = addresses.get(0);
                LatAdresse=returnedAddress.getLatitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return LatAdresse;
    }

    /**
     * Get longitude of target address
     * @param searchedAddress literal expression of target address
     * @return double
     */
    public double getLongitudeFromAddress(String searchedAddress){

        double LongAdresse=0;
        List<Address> addresses = null;
        Geocoder geo = new Geocoder(this.mContext, Locale.getDefault());

        try {
            addresses = geo.getFromLocationName(searchedAddress,1);
            if(addresses!=null)
            {
                Address returnedAddress = addresses.get(0);
                LongAdresse=returnedAddress.getLongitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return LongAdresse;
    }

//    public double getAltitudeFromAddress(String searchedAddress){
//    }

    /**
     * Get distance between two geographical points
     * @param LatTargetSource latitude of source point
     * @param LongTargetSource longitude of source point
     * @param LatTargetCible latitude of target point
     * @param LongTargetCible longitude of target point
     * @return double
     */
    public double getDistanceBetweenPoints(double LatTargetSource,
                                           double LongTargetSource,
                                           double LatTargetCible,
                                           double LongTargetCible){
        Location locationA = new Location("point A");
        locationA.setLatitude(LatTargetSource);
        locationA.setLongitude(LongTargetSource);
        Location locationB = new Location("point B");
        locationB.setLatitude(LatTargetCible);
        locationB.setLongitude(LongTargetCible);
        float distance=locationA.distanceTo(locationB);
        return distance;
    }

    /**
     * Get distance between two literal addresses
     * @param adressSource address of source
     * @param adressCible address of target
     * @return double
     */
    public double getDistanceEntreDeuxAdresse(String adressSource, String adressCible){
        Location locationA = new Location("point A");
        locationA.setLatitude(getLatitudeFromAddress(adressSource));
        locationA.setLongitude(getLongitudeFromAddress(adressSource));
        Location locationB = new Location("point B");
        locationB.setLatitude(getLatitudeFromAddress(adressCible));
        locationB.setLongitude(getLongitudeFromAddress(adressCible));
        float distance=locationA.distanceTo(locationB);
        return distance;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
