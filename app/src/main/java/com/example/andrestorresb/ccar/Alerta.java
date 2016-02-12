package com.example.andrestorresb.ccar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class Alerta extends AppCompatActivity {

    ListView listCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerta);

        //Hide Top Bar
        this.getSupportActionBar().hide();

        //Get listview to show
        this.listCar = (ListView)findViewById(R.id.listView);

        //Generate array of cars
        ArrayList<Vehiculo> slist = new ArrayList<Vehiculo>();

        //Get data from firebase (brand, color, plates)
        slist.add(new Vehiculo("Ferrari","Rojo", "FAW-ASF-RE"));
        MyAdapter a = new MyAdapter(this, slist);

        //Set adapter to list of cars
        this.listCar.setAdapter(a);
    }
}
