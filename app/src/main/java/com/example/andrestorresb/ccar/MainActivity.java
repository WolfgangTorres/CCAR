package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback{

    FloatingActionButton fab,fab2;
    TextView txtV,txtV2;
    GoogleMap map;
    Boolean flagProtegido=false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.fab=(FloatingActionButton)findViewById(R.id.fab);
        this.fab2=(FloatingActionButton)findViewById(R.id.fab2);
        this.txtV=(TextView)findViewById(R.id.protegido);
        this.txtV2=(TextView)findViewById(R.id.metros);
        this.fab.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
        this.fab2.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

    }

    public void protegido(View v){
        if(this.flagProtegido==false) {
            this.txtV.setText("Protegido");
            this.flagProtegido=true;
        }
        else if(this.flagProtegido==true){
            this.txtV.setText("No Protegido");
            this.flagProtegido=false;
        }
    }

    public void localizado(View v){
        this.txtV.setText("Localizado");
        this.txtV2.setText("420 m de ti");
        this.fab.hide();
        Intent i=new Intent(this,Alerta.class);
        startActivity(i);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }
}
