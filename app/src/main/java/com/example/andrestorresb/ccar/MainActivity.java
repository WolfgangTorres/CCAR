package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback{

    ImageButton protectionButton, localizationButton;
    TextView statusProtectionLabel,txtV2;
    GoogleMap map;
    Boolean statusProtection = false;

    final String PROTECTION_ON = "Protegido";
    final String PROTECTION_OFF = "No Protegido";
    
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
        this.statusProtectionLabel = (TextView)findViewById(R.id.statusProtection);
        this.txtV2 = (TextView)findViewById(R.id.metros);

        //Generate google map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

    }

    public void protect(View v){
        //Not Protected; therefore protect
        if(!this.statusProtection){
            //Change status label
            this.statusProtectionLabel.setText(PROTECTION_ON);

            //Change protectionButton to lock ON
            this.protectionButton.setBackgroundResource(R.drawable.lock_on);

            //Change flag
            this.statusProtection = true;

        }else{
            //Protected; therefore unprotect

            //Change status label
            this.statusProtectionLabel.setText(PROTECTION_OFF);

            //Change protectionButton to lock OFF
            this.protectionButton.setBackgroundResource(R.drawable.lock_off);

            //Change flag
            this.statusProtection = false;
        }
    }

    public void localizado(View v) {
        /*
        this.txtV.setText("Localizado");
        this.txtV2.setText("420 m de ti");
        this.fab.hide();
        Intent i = new Intent(this, Alerta.class);
        startActivity(i);
        */
        //Would be better to make a another activity for localization
        Toast.makeText(this, "Localizando...", Toast.LENGTH_SHORT).show();
        this.localizationButton.setBackgroundResource(R.drawable.localization_on);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }
}
