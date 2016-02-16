package com.example.andrestorresb.ccar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, Firebase.AuthResultHandler, ValueEventListener, JSONRequest.JSONListener{

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
    private final String FIREBASE_URL = "https://ccar-app.firebaseio.com/monitor";
    private final String FIREBASE_TOKEN = "EIiCIYN5Ef7WXqr2VTdwBYNF1uZQ0HMtx73YhoeP";

    //Firebase status
    private int actionFirebase = 0;
    private final int GET_CAR_LOCATION = 0,
                      GET_DEVICE_PROTECTION = 1,
                      SET_DEVICE_PROTECTION = 2;

    //CCAR Platform paths
    private final String DEVICES = "/devices";
    private final String USERS = "/users";

    //Device
    private double lat = 0.0,
                   lon = 0.0;
    private int timeLastLocation = 0;
    private String deviceID = ""; //Retrieve somehow from CCAR Platform (at login)
    private String userID = "";

    private JSONObject response;

    
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

        //Initial Configuration
        this.init();

        //Generate google map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void init(){
        //Get parameters from LoginActivity
        Intent i = getIntent();

        //Get user ID
        this.userID = i.getStringExtra("userID");

        //Get user devices (only first one)
        try {
            JSONArray devices = new JSONArray(i.getStringExtra("devices"));

            //Get first device (if length > 0)
            if(devices.length() > 0) {
                this.deviceID = devices.getString(0);
            }else {
                this.deviceID = "";
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Avoid changing due to no device owned
        this.noDevices();

        //Get Device Location
        //get through map init

        //Get & Set Device Protection
        this.getDeviceProtection();

        //Set user info
    }

    //Change state of components when protect ON || OFF
    private void componentsOfProtect(boolean status){
        if(status){
            //Notify user change
            this.ShowToast(PROTECTION_ON, Toast.LENGTH_SHORT);

            //Change status label
            this.statusLabel.setText(PROTECTION_ON);

            //Change protectionButton to lock ON
            this.protectionButton.setBackgroundResource(R.drawable.lock_on);
        }else{
            //Notify user change
            this.ShowToast(PROTECTION_OFF, Toast.LENGTH_SHORT);

            //Change status label
            this.statusLabel.setText(PROTECTION_OFF);

            //Change protectionButton to lock OFF
            this.protectionButton.setBackgroundResource(R.drawable.lock_off);
        }
    }

    //Avoid changing due to no device owned
    private void noDevices(){
        //Avoid changing due to no device owned
        if(this.deviceID.equals("")){
            //Notify user change
            this.ShowToast("Aún no tienes un dispositivo", Toast.LENGTH_SHORT);

            return;
        }
    }

    public void protect(View v){
        this.actionFirebase = SET_DEVICE_PROTECTION;

        //Avoid changing due to no device owned
        this.noDevices();

        //Not Protected; therefore protect
        if(!this.statusProtection){
            //Change flag
            this.statusProtection = true;

            //Send request to CCAR Platform
            String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/protectCar.php?devID=" + this.deviceID;
            new JSONRequest(this, this).execute(url);
        }else{
            //Protected; therefore unprotect
            //Change flag
            this.statusProtection = false;

            //Send request to CCAR Platform
            String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/unprotectCar.php?devID=" + this.deviceID;
            new JSONRequest(this, this).execute(url);
        }
    }

    public void locate(View v) {

        //Avoid changing due to no device owned
        this.noDevices();

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
        if(!this.deviceID.equals("")) this.fb.child(DEVICES + "/" + this.deviceID + "/location").addValueEventListener(this);
    }

    //Retrieve Device Protection from CCAR Platform
    private void getDeviceProtection(){
        this.actionFirebase = GET_DEVICE_PROTECTION;

        //Request Device Protection from CCAR Platform
        String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/getDeviceProtection.php?devID=" + this.deviceID;
        new JSONRequest(this, this).execute(url);

        /*
            Result:
                true: Protection ON
                false: Protection OFF
         */
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
                //Set location of device (firebase pure because realtime)
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

        switch (this.actionFirebase){
            case GET_DEVICE_PROTECTION:

                try {
                    JSONObject jsonobj = array;

                    //Set protection
                    if(jsonobj.getString("response").equals("true")) this.statusProtection = true;
                    else this.statusProtection = false;

                    this.componentsOfProtect(this.statusProtection);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;

            case SET_DEVICE_PROTECTION:

                try{
                    JSONObject jsonobj = array;

                    //Change status of components when success call
                    if(jsonobj.getString("response").equals("OK")) this.componentsOfProtect(this.statusProtection);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }
    }
}
