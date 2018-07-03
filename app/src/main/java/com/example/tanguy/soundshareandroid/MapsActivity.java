package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.tanguy.soundshareandroid.models.Friend;
import com.example.tanguy.soundshareandroid.models.GPSTracker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Map page for localising the user and its friends' positions
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FriendManagement friendManager = new FriendManagement();
    private ArrayList<Friend> friendList = new ArrayList<>();

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
            int height = 100;
            int width = 100;
            BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.logo_miniature);
            Bitmap b=bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            mMap.addMarker(new MarkerOptions().position(pos).title("I'm here !").icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
//            float zoomLevel = 16.0f;
            Map<String, Object> positionsFriends = this.getPositionsFriends();
            ArrayList<Double> latitudes = (ArrayList<Double>) positionsFriends.get("latitudes");
            ArrayList<Double> longitudes = (ArrayList<Double>) positionsFriends.get("longitudes");
            mMap.addMarker(new MarkerOptions().position(new LatLng(40, 40)));
            if(latitudes.size()>0){
                for (int i = 0; i<latitudes.size(); i++){
                    double lat = latitudes.get(i);
                    double lon = longitudes.get(i);
                    LatLng position = new LatLng(lat, lon);
                    mMap.addMarker(new MarkerOptions().position(position).title(this.friendManager.getFriendList().get(i).getUsername()));
                }
            }
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoomLevel));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            Toast.makeText(getApplicationContext(), "Votre position est  \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        }else{
            gps.showSettingsAlert();
        }
    }

    public Map<String, Object> getPositionsFriends(){
        Map<String, Object> pos = new HashMap<>();
        final ArrayList<Double> lat = new ArrayList<>();
        final ArrayList<Double> lon = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        friendManager.getFriends();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            final String userID = currentUser.getUid();

            // Get the list of user's friends from database
            db.collection("users").document(userID).collection("friends")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> friendJson = document.getData();

                                    String ID = document.getId();
                                    String username = (String) friendJson.get("username");
                                    String email = (String) friendJson.get("email");
                                    Friend friend = new Friend(ID, email, username);
                                    friendList.add(friend);
                                    Log.e("CHECK_1", " " + friendList.size());
                                }
                            } else {
                                Log.e("ERROR", "error getting friends" + task.getException());
                            }
                        }
                    });
//        ArrayList<Friend> friends = this.friendManager.getFriendList();
            Log.e("CHECK_2", " " + friendList.size());
            for (int i = 0; i < friendList.size(); i++) {
                Friend friend = friendList.get(i);
                String friendID = friend.getID();
                db.collection("users").document(friendID).collection("position").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Map<String, Object> positionJson = document.getData();
                                        double latitude = (double) positionJson.get("latitude");
                                        double longitude = (double) positionJson.get("longitude");
                                        lat.add(latitude);
                                        lon.add(longitude);
                                        Log.e("GET LOCATIONS", "Succeeded");
                                    }
                                } else {
                                    Log.e("ERROR", "error getting positions" + task.getException());
                                }
                            }
                        });
            }
            pos.put("latitudes", lat);
            pos.put("longitudes", lon);
        }
        return pos;
    }
}
