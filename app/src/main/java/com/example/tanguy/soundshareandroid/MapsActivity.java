package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.tanguy.soundshareandroid.models.Friend;
import com.example.tanguy.soundshareandroid.models.GPSTracker;
import com.example.tanguy.soundshareandroid.tools.map.AsyncTaskLoadImage;
import com.example.tanguy.soundshareandroid.tools.map.BitmapManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

/**
 * Map page for localising the user and its friends' positions
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Friend> friendList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<DocumentSnapshot> lastSongs = new ArrayList<>();

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
            final double latitude = gps.getLatitude();
            final double longitude = gps.getLongitude();
            final LatLng mpos = new LatLng(latitude, longitude);
            int height = 100;
            int width = 100;
            BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.logo_miniature);
            Bitmap b=bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            mMap.addMarker(new MarkerOptions().position(mpos).title("I'm here !").icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
//            float zoomLevel = 16.0f;
//            Map<String, Object> positionsFriends = this.getPositionsFriends();
//            Map<String, Object> pos = new HashMap<>();
//            final ArrayList<Double> lat = new ArrayList<>();
//            final ArrayList<Double> lon = new ArrayList<>();

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
                                    }
                                    for (int i = 0; i < friendList.size(); i++) {
                                        Friend fr = friendList.get(i);
                                        final String friendID = fr.getID();
                                        final String username = fr.getUsername();

                                        db.collection("users").document(friendID).collection("marker").document("last song")
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        String title;
//                                                        String artist;
                                                        final String coverurl;
//                                                        String storageID;
                                                        Bitmap icon;
                                                        if (task.isSuccessful()){
                                                            DocumentSnapshot document = task.getResult();
                                                            if(document.exists()){
                                                                Map<String, Object> songJson = document.getData();
                                                                title = (String) songJson.get("TITLE");
                                                                Log.e("TEST2", title);
//                                                                artist = (String) songJson.get("ARTIST");
                                                                coverurl = (String) songJson.get("COVERURL");
//                                                                storageID = (String) songJson.get("STORAGEID");
//                                                                icon = BitmapManager.getBitmapFromURL(coverurl);

                                                                Thread thread = new Thread(new Runnable(){
                                                                    @Override
                                                                    public void run(){
                                                                        URL url ;
                                                                        Bitmap bitmap = null;
                                                                        try {
                                                                            url = new URL(coverurl);
                                                                            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                        final Bitmap finalBitmap = bitmap;
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                int height = 100;
                                                                                int width = 100;
                                                                                assert finalBitmap != null;
                                                                                Bitmap icon = Bitmap.createScaledBitmap(finalBitmap, width, height, false);
                                                                                final Bitmap finalIcon = icon;
                                                                                db.collection("users").document(friendID).collection("marker")
                                                                                        .document("position")
                                                                                        .get()
                                                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                            LatLng position = null;
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Log.e("TEST4", "QQN");
                                                                                                    DocumentSnapshot document = task.getResult();
                                                                                                    if (document.exists()){
                                                                                                        Map<String, Object> positionJson = document.getData();
                                                                                                        double lat = (double) positionJson.get("latitude");
                                                                                                        double lon = (double) positionJson.get("longitude");
                                                                                                        Log.e("TEST5", "QQN" + lat);
                                                                                                        position = new LatLng(lat, lon);
                                                                                                        mMap.addMarker(new MarkerOptions().position(position).title(username).icon(BitmapDescriptorFactory.fromBitmap(finalIcon)));
                                                                                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mpos));
                                                                                                        Toast.makeText(getApplicationContext(), "Votre position est  \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                Log.w("ERROR", "FAILURE MARKER", e);
                                                                                            }
                                                                                        });
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                                thread.start();

//////                                                                try {
//////                                                                    URL url = new URL(coverurl);
////////                                                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
////////                                                                    connection.setRequestMethod("GET");
////////                                                                    connection.setReadTimeout(15*1000);
////////                                                                    connection.setDoInput(true);
//////
//////                                                                    icon = BitmapFactory.decodeStream((InputStream)url.getContent());
//////
////////                                                                    connection.connect();
//////                                                                    Log.e("TEST3e", "ok");
////////                                                                    InputStream input = connection.getInputStream();
//////                                                                    Log.e("TEST3f", "ok");
////////                                                                    icon = BitmapFactory.decodeStream(input);
//////                                                                    int height = 100;
//////                                                                    int width = 100;
//////                                                                    assert icon != null;
//////                                                                    icon = Bitmap.createScaledBitmap(icon, width, height, false);
//////                                                                    final Bitmap finalIcon = icon;
//////                                                                    db.collection("users").document(friendID).collection("marker")
//////                                                                            .document("position")
//////                                                                            .get()
//////                                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//////                                                                                LatLng position = null;
//////                                                                                @Override
//////                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//////                                                                                    if (task.isSuccessful()){
//////                                                                                        Log.e("TEST4", "QQN");
//////                                                                                        DocumentSnapshot document = task.getResult();
//////                                                                                        if (document.exists()){
//////                                                                                            Map<String, Object> positionJson = document.getData();
//////                                                                                            double lat = (double) positionJson.get("latitude");
//////                                                                                            double lon = (double) positionJson.get("longitude");
//////                                                                                            Log.e("TEST5", "QQN" + lat);
//////                                                                                            position = new LatLng(lat, lon);
//////                                                                                            mMap.addMarker(new MarkerOptions().position(position).title(username).icon(BitmapDescriptorFactory.fromBitmap(finalIcon)));
//////                                                                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(mpos));
//////                                                                                            Toast.makeText(getApplicationContext(), "Votre position est  \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
//////                                                                                        }
//////                                                                                    }
//////                                                                                }
//////                                                                            })
//////                                                                            .addOnFailureListener(new OnFailureListener() {
//////                                                                                @Override
//////                                                                                public void onFailure(@NonNull Exception e) {
//////                                                                                    Log.w("ERROR", "FAILURE MARKER", e);
//////                                                                                }
//////                                                                            });
////                                                                } catch (IOException e) {
////                                                                    Log.e("ERROREXCEPTION", "AU SECOURS");
//                                                                }
                                                            }
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("ERROR", "FAILURE LAST SONG", e);
                                                    }
                                                })
                                        ;

//                                        db.collection("users").document(friendID).collection("marker").get()
//                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                        LatLng position = null;
//                                                        String title;
//                                                        String artist;
//                                                        String coverurl = null;
//                                                        String storageID;
//                                                        BitmapManager bitmapManager = new BitmapManager();
//                                                        Bitmap icon = null;
//                                                        if (task.isSuccessful()){
//                                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                                                if (document.getId() == "last song"){
//                                                                    Map<String, Object> songJson = document.getData();
//                                                                    title = (String) songJson.get("TITLE");
//                                                                    artist = (String) songJson.get("ARTIST");
//                                                                    coverurl = (String) songJson.get("COVERURL");
//                                                                    storageID = (String) songJson.get("STORAGEID");
//                                                                    icon = bitmapManager.getBitmapFromURL(coverurl);
//                                                                    int height = 100;
//                                                                    int width = 100;
//                                                                    icon = Bitmap.createScaledBitmap(icon, width, height, false);
//                                                                } else {
//                                                                    Map<String, Object> positionJson = document.getData();
//                                                                    double lat = (double) positionJson.get("latitude");
//                                                                    double lon = (double) positionJson.get("longitude");
//                                                                    position = new LatLng(lat, lon);
//                                                                    mMap.addMarker(new MarkerOptions().position(position).title(username).icon(BitmapDescriptorFactory.fromBitmap(icon)));
//                                                                }
//                                                            }
//                                                            Log.e("ADD MARKERS", "Succeeded");
//                                                        } else {
//                                                            Log.e("ERROR", "error adding markers" + task.getException());
//                                                        }
//                                                    }
//                                                });

//                                        db.collection("users").document(friendID).collection("marker").document("position")
//                                                .get()
//                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                                        if(task.isSuccessful()){
//                                                            DocumentSnapshot document = task.getResult();
//                                                            Map<String, Object> positionJson = document.getData();
//                                                            double lat = (double) positionJson.get("latitude");
//                                                            double lon = (double) positionJson.get("longitude");
//                                                            LatLng position = new LatLng(lat, lon);
//                                                            mMap.addMarker(new MarkerOptions().position(position).title(username));
//                                                            Log.e("GET LOCATIONS", "Succeeded");
//                                                        } else {
//                                                            Log.e("ERROR", "error getting positions" + task.getException());
//                                                        }
//                                                    }
//                                                });
                                    }
                                } else {
                                    Log.e("ERROR", "error getting friends" + task.getException());
                                }
                            }
                        });
            }
        }else{
            gps.showSettingsAlert();
        }
    }
}
