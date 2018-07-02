package com.example.tanguy.soundshareandroid;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.tanguy.soundshareandroid.models.GPSTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Map page for localising the user and its friends' positions
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        GPSTracker gps = new GPSTracker(MapsActivity.this);
        if(gps.canGetLocation()){
//            Location location = gps.getLocation();
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
//            Location location = gps.getLocation();
//            double latitude = location.getLatitude();
//            double longitude= location.getLongitude();
            LatLng pos = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(pos).title("I'm here !"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            Toast.makeText(getApplicationContext(), "Votre position est  \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        }else{
            gps.showSettingsAlert();
        }
    }
}
