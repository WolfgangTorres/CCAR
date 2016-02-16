package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class Mycars extends AppCompatActivity implements JSONRequest.JSONListener {

    TextView modelo;
    ImageButton imgbtn;
    Intent i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycars);

        this.getSupportActionBar().setTitle("Borrar vehiculos");

        this.modelo = (TextView)findViewById(R.id.modelo);
        this.imgbtn = (ImageButton)findViewById(R.id.delete);
        this.i = getIntent();


    }

    public void eliminar(View v){
        new JSONRequest(this,this).execute("http://renatogutierrez.com/apps/CCAR/Plataforma/unlinkDevice.php?devID="+this.i.getStringExtra("deviceID")+"&userID="+this.i.getStringExtra("userID"));
        finish();
    }

    @Override
    public void doSomething(JSONObject array) {
        if(array!=null){
            Toast.makeText(this,"Se Borro Satisfactoriamente",Toast.LENGTH_SHORT).show();
        }
    }
}
