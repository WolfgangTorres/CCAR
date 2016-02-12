package com.example.andrestorresb.ccar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class Alerta extends AppCompatActivity {

    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerta);


        this.list=(ListView)findViewById(R.id.listView);
        ArrayList<Vehiculo> slist=new ArrayList<Vehiculo>();
        slist.add(new Vehiculo("Ferrari","Rojo", "FAW-ASF-REW"));
        MyAdapter a=new MyAdapter(this, slist);
        this.list.setAdapter(a);
    }
}
