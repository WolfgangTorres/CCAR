package com.example.andrestorresb.ccar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.Distance;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, Firebase.AuthResultHandler, ValueEventListener, ChildEventListener, JSONRequest.JSONListener,NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private ImageButton protectionButton, localizationButton, cancelRoute;
    private TextView statusLabel,txtV2;
    private GoogleMap map;
    private Boolean statusProtection = false,
                    statusLocalization = false;

    private final String PROTECTION_ON = "Protegido",
                         PROTECTION_OFF = "No Protegido",
                         LOCALIZATION = "Localizando";
    private final int CODE = 0;

    double myLat, myLon,devLat,devLon;

    private FusedLocationProviderApi fusedLocationProviderApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private final String GoogleServerApiKey = "AIzaSyAnB50FhXE1HqnFveR6uXnYzKQO8vYrEfY";
    Polyline newPolyline;
    Marker marker;



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
                      SET_DEVICE_PROTECTION = 2,
                      LISTEN_CRISTALAZO_ALERT = 3;
    private boolean newChildAdded = false;

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

    private DrawerLayout menu;

    private boolean locacion1=true;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.menu=(DrawerLayout)findViewById(R.id.menu);
        NavigationView navigationView=(NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Left button
        this.localizationButton = (ImageButton)findViewById(R.id.localizationButton);
        this.localizationButton.setBackgroundResource(R.drawable.localization_off);

        //Right Button
        this.protectionButton = (ImageButton)findViewById(R.id.protectionButton);
        this.protectionButton.setBackgroundResource(R.drawable.lock_off);

        //Middle Button
        this.cancelRoute=(ImageButton)findViewById(R.id.cancelRoute);
        this.cancelRoute.setVisibility(View.INVISIBLE);

        //Status Protection Label
        this.statusLabel = (TextView)findViewById(R.id.statusLabel);
        this.txtV2 = (TextView)findViewById(R.id.metros);

        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (this.googleApiClient != null) {
            Log.d("CONECTADO","GOOGLEAPI");
            this.googleApiClient.connect();
        }

        //Initialize Firebase
        this.initializeFirebase();

        //Initial Configuration
        this.init();

        //Generate google map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Toast.makeText(this, "On Resumen!", Toast.LENGTH_SHORT).show();

        String endpoint = "http://renatogutierrez.com/apps/CCAR/Plataforma/getDevices.php?userID=" + this.userID;

        Log.d("getDevices", endpoint);

        new JSONRequest(this, new JSONRequest.JSONListener() {
            @Override
            public void doSomething(JSONObject array) {
                if(array != null){
                    try {
                        JSONArray devices = array.getJSONArray("devices");

                        Log.d("getDevices", devices.toString() + " , " + devices.length());

                        if(devices.length() > 0){
                            deviceID = devices.get(0).toString();

                            //Avoid changing due to no device owned
                            noDevices();

                            //Get Device Location
                            //get through map init

                            //Get & Set Device Protection
                            getDeviceProtection();

                            //Set user info

                            //Listen Cristalazo Alert
                            listenCristalazoAlert();

                            getDeviceLocationOnce();
                        }else{
                            //Avoid changing due to no device owned
                            noDevices();

                            cleanMap();

                            Log.d("getDevices", "Clean Mape!");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).execute(endpoint);
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

        //Listen Cristalazo Alert
        this.listenCristalazoAlert();
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.menu);
        drawer.openDrawer(GravityCompat.START);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //Set position of car (from CCAR Platform)
        this.map = map;

        //Get & Set car location from CCAR Platform
        if(this.locacion1) {
            this.getDeviceLocation();
        }
    }

    //Generate notification
    private void ShowToast(String msg, int duration){
        Toast.makeText(this, msg, duration).show();
    }

    //Set position of car (from CCAR Platform)
    private void carLocate(){
        float zoom = 15f; //Street level

        //Center map
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new com.google.android.gms.maps.model.LatLng(this.lat, this.lon), zoom));

        //Remove previous markers; only on screen current marker
        if(this.prevMarker != null) this.prevMarker.remove();

        //Add custom marker
        this.currentMarker = this.map.addMarker(new MarkerOptions()
                                    .position(new com.google.android.gms.maps.model.LatLng(this.lat, this.lon))
                                    .title("My Car")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_car_location)));

        this.prevMarker = this.currentMarker;
    }

    //Clean map
    private void cleanMap(){
        this.currentMarker.remove();
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
        this.locacion1=false;
        this.actionFirebase = GET_CAR_LOCATION;

        //Get location from CCAR Platform
        if(!this.deviceID.equals("")) this.fb.child(DEVICES + "/" + this.deviceID + "/location").addValueEventListener(this);
    }

    private void getDeviceLocationOnce(){
        if(!this.deviceID.equals("")) this.fb.child(DEVICES + "/" + this.deviceID + "/location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                actionFirebase = GET_CAR_LOCATION;
                initialPosition = true;

                setLocationDevice(dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
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

    private void listenCristalazoAlert(){

        //Listen event of Cristalazo Alert
        this.actionFirebase = LISTEN_CRISTALAZO_ALERT;

        if( !this.deviceID.equals("") ) this.fb.child(DEVICES + "/" + this.deviceID + "/alerts").limitToLast(1).addChildEventListener(this);
    }

    //Firebase callback on Auth
    @Override
    public void onAuthenticated(AuthData authData) {
        //this.ShowToast("Auth OK", Toast.LENGTH_LONG);
    }

    @Override
    public void onAuthenticationError(FirebaseError firebaseError) { this.ShowToast("Fail to Auth", Toast.LENGTH_LONG); }

    private void setLocationDevice(DataSnapshot dataSnapshot){
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
    }

    //Firebase callback on SingleEvent
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if(dataSnapshot == null) return;

        //Detect action request to Firebase
        switch (this.actionFirebase){
            case GET_CAR_LOCATION:
                setLocationDevice(dataSnapshot);

                break;
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        //Fire only when cristalazo alert occurs; avoid initial retrieve
        if(this.newChildAdded && this.statusProtection) {
            //Initialize Cristalazo Alert Activity
            final Intent i = new Intent(this, Alerta.class);

            //Send cristalazo alert data
            i.putExtra("alert", (Serializable) dataSnapshot.getValue());

            //Get car info
            final String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/getDeviceCarInfo.php?devID=" + this.deviceID + "&userID=" + this.userID;
            new JSONRequest(this, new JSONRequest.JSONListener() {
                @Override
                public void doSomething(JSONObject array) {
                    try {
                        //Sent car info
                        i.putExtra("car", array.getJSONObject("response").toString());

                        //Initialize Cristalazo Alert Activity
                        startActivityForResult(i,CODE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).execute(url);
        }

        this.newChildAdded = true;
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0){
            //Result from MyCars
            if(data != null){
                //Reload device on map
                String deviceEdited = data.getStringExtra("deviceID");

                this.deviceID = deviceEdited;
            }
        }
    }

    private void viewCars(){
        Intent i=new Intent(this, Mycars.class);
        i.putExtra("userID", this.userID);
        i.putExtra("deviceID",this.deviceID);
        startActivityForResult(i, 0);
    }

    private void openProfile(){
        Intent i=new Intent(this, ProfileFrag.class);
        i.putExtra("userID",this.userID);
        startActivity(i);
    }

    private void logout(){
        //Destroy properties xml (auto login)
        new File(getFilesDir(), LoginActivity.credentialsFile).delete();

        //End Main Activity
        finish();

        //Start Login Activity
        Intent i=new Intent(this,LoginActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //Profile clicked
        if (id == R.id.profile) {
            this.openProfile();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.menu);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.car) {
            //Cars clicked
            this.viewCars();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.menu);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.logout) {
            //Logout clicked
            this.logout();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.menu);
            drawer.closeDrawer(GravityCompat.START);
        } else if(id==R.id.informacion){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.menu);
        }

        return true;
    }


    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode) {
        if(this.marker!=null){
            this.marker.remove();
        }
        this.marker=this.map.addMarker(new MarkerOptions().position(new com.google.android.gms.maps.model.LatLng(this.myLat, this.myLon)));
        this.map.moveCamera(CameraUpdateFactory.newLatLng(new com.google.android.gms.maps.model.LatLng(this.myLat, this.myLon)));
        Map<String, String> map = new HashMap<String, String>();
        map.put(GetDirections.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(GetDirections.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(GetDirections.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(GetDirections.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(GetDirections.DIRECTIONS_MODE, mode);

        GetDirections asyncTask = new GetDirections(this);
        asyncTask.execute(map);
    }

    public void handleGetDirectionsResult(ArrayList directionPoints) {
        PolylineOptions rectLine = new PolylineOptions().width(10).color(Color.BLUE);
        for(int i = 0 ; i < directionPoints.size() ; i++)
        {
            rectLine.add((com.google.android.gms.maps.model.LatLng) directionPoints.get(i));
        }
        if(this.newPolyline!=null){
            this.newPolyline.remove();
        }
        this.newPolyline = this.map.addPolyline(rectLine);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == this.CODE && resultCode == Activity.RESULT_OK){
            locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            fusedLocationProviderApi = LocationServices.FusedLocationApi;


            this.myLat=data.getDoubleExtra("latUser",0);
            this.myLon=data.getDoubleExtra("lonUser",0);
            this.devLat=data.getDoubleExtra("latDev",0);
            this.devLon=data.getDoubleExtra("lonDev",0);
            this.localizationButton.setVisibility(View.INVISIBLE);
            this.protectionButton.setVisibility(View.INVISIBLE);
            this.cancelRoute.setVisibility(View.VISIBLE);
            this.setDistance(new LatLng(this.myLat, this.myLon), new LatLng(this.devLat, this.devLon), true);
            this.findDirections(this.myLat,this.myLon,this.devLat,this.devLon,GMapRuta.MODE_WALKING);
            this.startLocationUpdates();
        }

    }

    private void setDistanceHelper(Location location){
        //Get current location (smartphone)
        this.myLat = location.getLatitude();
        this.myLon = location.getLongitude();

        Log.d("(last) Current Location", this.myLat + ", " + this.myLon);

        this.findDirections(this.myLat, this.myLon, this.devLat, this.devLon, GMapRuta.MODE_WALKING);
        LatLng userLocation = new LatLng( this.myLat, this.myLon );
        LatLng deviceLocation = new LatLng( this.devLat, this.devLon );
        this.setDistance(userLocation, deviceLocation, true);
    }

    private void setDistance(LatLng origin, LatLng destination, boolean isWalking) {
        String distance = this.getDistance(origin, destination, isWalking);
        this.statusLabel.setText(distance);
    }

    private String getDistance(LatLng origin, LatLng destination, boolean isWalking) {
        GeoApiContext context = new GeoApiContext().setApiKey(GoogleServerApiKey);
        try {
            TravelMode tm = (isWalking) ? TravelMode.WALKING : TravelMode.DRIVING;

            //Get Distance between two points (walking)
            DistanceMatrix distanceMatrix = DistanceMatrixApi.newRequest(context)
                    .origins(origin)
                    .destinations(destination)
                    .mode(tm)
                    .units(Unit.METRIC)
                    .await();

            Distance distance = distanceMatrix.rows[0].elements[0].distance;

            if(distance != null)
                //Exist distance
                return distanceMatrix.rows[0].elements[0].distance.toString();
            else
                return "- m";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "0 m";
    }

    @Override
    public void onLocationChanged(Location location) {
        this.setDistanceHelper(location);
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(this.googleApiClient, locationRequest, this);
    }

    public void stopRoute(View v){
        this.newPolyline.remove();
        this.marker.remove();
        this.protectionButton.setVisibility(View.VISIBLE);
        this.localizationButton.setVisibility(View.VISIBLE);
        this.cancelRoute.setVisibility(View.INVISIBLE);
        this.statusLabel.setText(PROTECTION_OFF);
        this.map.moveCamera(CameraUpdateFactory.newLatLng(new com.google.android.gms.maps.model.LatLng(this.devLat, this.devLon)));
        LocationServices.FusedLocationApi.removeLocationUpdates(
                this.googleApiClient, this);
    }
}
