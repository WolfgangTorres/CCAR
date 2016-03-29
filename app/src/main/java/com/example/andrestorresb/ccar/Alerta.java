package com.example.andrestorresb.ccar;

import com.google.maps.GeoApiContext;
import com.google.maps.DistanceMatrixApi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.maps.model.Distance;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Alerta extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    ListView listCar;
    ArrayList<Vehiculo> slist;

    private final int PERMISSION_ACCESS_FINE_LOCATION = 0;

    private LocationRequest locationRequest;
    private FusedLocationProviderApi fusedLocationProviderApi;
    private GoogleApiClient googleApiClient;

    private GoogleApiClient mLocationClient;

    private final String GoogleServerApiKey = "AIzaSyAnB50FhXE1HqnFveR6uXnYzKQO8vYrEfY";

    private double deviceLat,
            deviceLon,
            myLat,
            myLon;

    private TextView distanceLabel;
    private TextView addressLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerta);

        //Hide Top Bar
        this.getSupportActionBar().hide();

        //Get listview to show
        this.listCar = (ListView) findViewById(R.id.listView);
        this.distanceLabel = (TextView) findViewById(R.id.distance);
        this.addressLabel = (TextView) findViewById(R.id.address);

        //Generate array of cars
        this.slist = new ArrayList<Vehiculo>();

        //Get data from firebase (brand, color, plates)
        Intent i = getIntent();
        HashMap<String, ?> alert = (HashMap<String, ?>) i.getSerializableExtra("alert");

        //Get car info
        JSONObject carInfo = null;
        try {
            carInfo = new JSONObject(i.getStringExtra("car"));

            HashMap<String, String> car = new HashMap<>();
            car.put("brand", carInfo.get("brand").toString());
            car.put("color", carInfo.get("color").toString());
            car.put("model", carInfo.get("model").toString());
            car.put("plates", carInfo.get("plates").toString());

            this.slist.add(new Vehiculo(car.get("brand"), car.get("color"), car.get("plates")));

            //Create adapter
            MyAdapter a = new MyAdapter(this, this.slist);

            //Set adapter to list of cars
            this.listCar.setAdapter(a);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Get address where occurs
        HashMap<String, ?> location = (HashMap<String, ?>) alert.get("location");
        this.deviceLat = (Double) location.get("lat");
        this.deviceLon = (Double) location.get("lon");

        //Get Address from lat & lon
        Geocoder gc = new Geocoder(this);
        try {
            List<android.location.Address> a = gc.getFromLocation(this.deviceLat, this.deviceLon, 1);
            String address = a.get(0).getAddressLine(0);

            //Set address
            this.addressLabel.setText(address);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Get location of user (smartphone); auto set distance from user to device
        this.getUserLocation();
    }

    private void getUserLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(5);

        fusedLocationProviderApi = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    private void setDistance(LatLng origin, LatLng destination, boolean isWalking) {
        String distance = this.getDistance(origin, destination, isWalking);

        this.distanceLabel.setText(distance);
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
    public void onConnected(Bundle bundle) {

        //Verify permission of GPS (Access_Fine_Location)
        if (!checkPermission()) {

            //Request permission
            requestPermission();
        }else{
            //Permission Granted previous
            requestUserLocation();

            //For testing purposes on emulator; remove on real device
            //geo fix -103.448096 20.742338
            /*
            LatLng userLocation = new LatLng(20.742338, -103.448096);
            LatLng deviceLocation = new LatLng(this.deviceLat, this.deviceLon);

            boolean isWalking = true;
            this.setDistance(userLocation, deviceLocation, isWalking);
            */
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.setDistanceHelper( location );
    }

    private void setDistanceHelper(Location location){
        //Get current location (smartphone)
        this.myLat = location.getLatitude();
        this.myLon = location.getLongitude();

        Log.d("(last) Current Location", this.myLat + ", " + this.myLon);

        //Create LatLn object for both locations (user & device)
        LatLng userLocation = new LatLng( this.myLat, this.myLon );
        LatLng deviceLocation = new LatLng( this.deviceLat, this.deviceLon );

        //Route via walking
        boolean isWalking = true;

        //Set distance from user to device
        this.setDistance( userLocation, deviceLocation, isWalking );
    }

    private void requestUserLocation(){
        //Get last known location
        Location location = fusedLocationProviderApi.getLastLocation(googleApiClient);
        this.setDistanceHelper( location );

        //Listen for updated in location (but, only wants location to calculate distance, so only require once)
        //fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    /**
     * See if we have permissionf or locations
     *
     * @return boolean, true for good permissions, false means no permission
     */
    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Request permissions from the user
     */
    private void requestPermission(){

        /**
         * Previous denials will warrant a rationale for the user to help convince them.
         */
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this, "This app relies on location data for it's main functionality. Please enable GPS data to access all features.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Monitor for permission changes.
     *
     * @param requestCode passed via PERMISSION_REQUEST_CODE
     * @param permissions list of permissions requested
     * @param grantResults the result of the permissions requested
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /**
                     * We are good, turn on monitoring
                     */
                    if (checkPermission()) {
                        requestUserLocation();
                    } else {
                        requestPermission();
                    }
                } else {
                    /**
                     * No permissions, block out all activities that require a location to function
                     */
                    Toast.makeText(this, "Permission Not Granted.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}
}
