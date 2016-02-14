package com.example.andrestorresb.ccar;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, Firebase.AuthResultHandler, ValueEventListener,JSONRequest.JSONListener{

    private ImageButton protectionButton, localizationButton;
    private TextView statusLabel,txtV2;
    private GoogleMap map;
    private Boolean statusProtection = false,
                    statusLocalization = false;

    private final String PROTECTION_ON = "Protegido",
                         PROTECTION_OFF = "No Protegido",
                         LOCALIZATION = "Localizando";

    //Google maps
    private Marker currentMarker,
                   prevMarker;
    private boolean initialPosition = true;

    //Firebase credentials
    private Firebase fb;
    private final String FIREBASE_URL = "https://ccar-app.firebaseio.com/";
    private final String FIREBASE_TOKEN = "EIiCIYN5Ef7WXqr2VTdwBYNF1uZQ0HMtx73YhoeP";

    //Firebase status
    private int actionFirebase = 0;
    private final int GET_CAR_LOCATION = 0,
                      GET_DEVICE_PROTECTION = 1;

    //CCAR Platform paths
    private final String DEVICES = "monitor/devices";
    private final String USERS = "monitor/users";

    //Device
    private double lat = 0.0,
                   lon = 0.0;
    private int timeLastLocation = 0;
    private String deviceID = "renato"; //Retrieve somehow from CCAR Platform (at login)

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Left button
        this.localizationButton = (ImageButton)findViewById(R.id.localizationButton);
        this.localizationButton.setBackgroundResource(R.drawable.localization_off);

        //Right Button
        this.protectionButton = (ImageButton)findViewById(R.id.protectionButton);
        this.protectionButton.setBackgroundResource(R.drawable.lock_off);

        //Status Protection Label
        this.statusLabel = (TextView)findViewById(R.id.statusLabel);
        this.txtV2 = (TextView)findViewById(R.id.metros);

        //Initialize Firebase
        this.initializeFirebase();

        //Generate google map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("Latitud", this.lat + "");
        Log.d("Longitud", this.lon + "");
        Log.d("Time",this.timeLastLocation+"");
    }

    public void protect(View v){
        //Not Protected; therefore protect
        if(!this.statusProtection){
            //Change flag
            this.statusProtection = true;
            //Send request to CCAR Platform
            String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/protectCar.php?devID=" + this.deviceID;

            //Notify user change
            this.ShowToast(PROTECTION_ON, Toast.LENGTH_SHORT);

            //Change status label
            this.statusLabel.setText(PROTECTION_ON);

            //Change protectionButton to lock ON
            this.protectionButton.setBackgroundResource(R.drawable.lock_on);


        }else{
            //Protected; therefore unprotect
            //Change flag
            this.statusProtection = false;
            //Send request to CCAR Platform
            String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/unprotectCar.php?devID=" + this.deviceID;

            //Notify user change
            this.ShowToast(PROTECTION_OFF, Toast.LENGTH_SHORT);

            //Change status label
            this.statusLabel.setText(PROTECTION_OFF);

            //Change protectionButton to lock OFF
            this.protectionButton.setBackgroundResource(R.drawable.lock_off);

            //Change flag
            this.statusProtection = false;
        }
    }

    public void locate(View v) {

        //Locate Car
        if(!this.statusLocalization){
            //Hide protectionButton
            this.protectionButton.setVisibility(View.INVISIBLE);

            //Set status label
            this.statusLabel.setText(LOCALIZATION);

            //Change localizationButton to localization ON
            this.localizationButton.setBackgroundResource(R.drawable.localization_on);

            //Change flag
            this.statusLocalization = true;

            //Notify user change
            this.ShowToast(LOCALIZATION, Toast.LENGTH_SHORT);

            //Get & Set car location from CCAR Platform
            this.getDeviceLocation();

        }else{
            //Exit from localization

            //Show statusProtectionLabel, protectionButton
            this.protectionButton.setVisibility(View.VISIBLE);

            //Set status label to protection status
            if(this.statusProtection) this.statusLabel.setText(PROTECTION_ON);
            else this.statusLabel.setText(PROTECTION_OFF);

            //Change localizationButton to localization OFF
            this.localizationButton.setBackgroundResource(R.drawable.localization_off);

            //Change flag
            this.statusLocalization = false;
        }
    }

    public void menu(View v) {
        //Test: have to open slide menu
        /*
            Login

            (photo circle) Hi Name
            Profile
            My Cars

            Logout

         */
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //Set position of car (from CCAR Platform)
        this.map = map;

        //Get & Set car location from CCAR Platform
        this.getDeviceLocation();
    }

    //Generate notification
    private void ShowToast(String msg, int duration){
        Toast.makeText(this, msg, duration).show();
    }

    //Set position of car (from CCAR Platform)
    private void carLocate(){
        float zoom = 15f; //Street level

        //Center map
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(this.lat, this.lon), zoom));

        //Remove previous markers; only on screen current marker
        if(this.prevMarker != null) this.prevMarker.remove();

        //Add custom marker
        this.currentMarker = this.map.addMarker(new MarkerOptions()
                                    .position(new LatLng(this.lat, this.lon))
                                    .title("My Car")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_car_location)));

        this.prevMarker = this.currentMarker;
    }

    //Initialize Firebase
    private void initializeFirebase(){
        //Initialize Firebase
        Firebase.setAndroidContext(this);
        this.fb = new Firebase(FIREBASE_URL);

        //Auth in Firebase
        this.fb.authWithCustomToken(FIREBASE_TOKEN, this);
    }



    //Retrieve Device Location from CCAR Platform
    private void getDeviceLocation(){
        this.actionFirebase = GET_CAR_LOCATION;

        //Get location from CCAR Platform
        this.fb.child(DEVICES + "/" + this.deviceID + "/location").addValueEventListener(this);

        //Get location from CCAR Platform
        String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/getDeviceLocation.php?devID=" + this.deviceID;
        new JSONRequest(this,this).execute(url);

        Log.d("Latitud", this.lat + "");
        Log.d("Longitud",this.lon+"");
        Log.d("Time", this.timeLastLocation + "");

        /*
            Result:
                {
                    "lat":20.6747568,
                    "lon":-103.445001,
                    "time":1453178575

                }
         */
    }

    private void getDeviceProtection(){
        this.actionFirebase = GET_DEVICE_PROTECTION;

        //Get device protection from CCAR Platform
        String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/getDeviceProtection.php?devID=" + this.deviceID;

        /*
            Result:
                true: Protection ON
                false: Protection OFF
         */

        String response = "";

        if(response == "true"){
            this.statusProtection = true;
        }else{
            this.statusProtection = false;
        }
    }

    //Firebase callback on Auth
    @Override
    public void onAuthenticated(AuthData authData) {
        //this.ShowToast("Auth OK", Toast.LENGTH_LONG);
    }

    @Override
    public void onAuthenticationError(FirebaseError firebaseError) { this.ShowToast("Fail to Auth", Toast.LENGTH_LONG); }

    //Firebase callback on SingleEvent
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if(dataSnapshot == null) return;

        //Detect action request to Firebase
        switch (this.actionFirebase){
            case GET_CAR_LOCATION:
                //Set location of device
                this.lat = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                this.lon = Double.parseDouble(dataSnapshot.child("lon").getValue().toString());
                this.timeLastLocation = Integer.parseInt(dataSnapshot.child("time").getValue().toString());

                //Represent in map

                //When localization module is ON
                if(this.statusLocalization) this.carLocate();

                //When initialPosition is ON
                if(this.initialPosition){
                    this.carLocate();
                    this.initialPosition = false;
                }

                break;
        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }

    @Override
    public void doSomething(JSONObject array) {
        //el for por si hay mas de un solo lat,lon y time
        //for(int i=0;i<array.length()-1;i++) {
            try {
                JSONObject jsonobj = array;
                this.lat=jsonobj.getDouble("lat");
                this.lon=jsonobj.getDouble("lon");
                this.timeLastLocation=jsonobj.getInt("time");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        //}
    }
}
