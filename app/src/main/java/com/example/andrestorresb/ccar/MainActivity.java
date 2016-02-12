package com.example.andrestorresb.ccar;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback{

    ImageButton protectionButton, localizationButton;
    TextView statusLabel,txtV2;
    GoogleMap map;
    Boolean statusProtection = false,
            statusLocalization = false;

    final String PROTECTION_ON = "Protegido",
                 PROTECTION_OFF = "No Protegido",
                 LOCALIZATION = "Localizando";
    
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

        //Generate google map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void protect(View v){
        //Not Protected; therefore protect
        if(!this.statusProtection){
            //Notify user change
            this.ShowToast(PROTECTION_ON, Toast.LENGTH_SHORT);

            //Change status label
            this.statusLabel.setText(PROTECTION_ON);

            //Change protectionButton to lock ON
            this.protectionButton.setBackgroundResource(R.drawable.lock_on);

            //Change flag
            this.statusProtection = true;

        }else{
            //Protected; therefore unprotect

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

            //Set position of car (from CCAR Platform)
            this.carLocate();

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
        this.carLocate();
    }

    //Generate notification
    private void ShowToast(String msg, int duration){
        Toast.makeText(this, msg, duration).show();
    }

    //Set position of car (from CCAR Platform)
    private void carLocate(){
        //Get position from CCAR Platform
        double lat = 20.735014,
                lon = -103.456280;
        float zoom = 15f; //Street level

        //Center map
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), zoom));

        //Add custom marker
        this.map.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lon))
                .title("My Car")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_car_location)));
    }
}
