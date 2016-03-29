package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class Editar extends ActionBarActivity {

    EditText modelo,color,placas;
    Button boton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.modelo=(EditText)findViewById(R.id.editModelo);
        this.color=(EditText)findViewById(R.id.editColor);
        this.placas=(EditText)findViewById(R.id.editPlacas);
        this.boton=(Button)findViewById(R.id.btnGuardar);

        Intent i=getIntent();
        ArrayList array;
        array=i.getStringArrayListExtra("carro");
        this.modelo.setText(array.get(0).toString());
        this.color.setText(array.get(1).toString());
        this.placas.setText(array.get(2).toString());
    }

    public void guardar(View v){
        finish();
    }
}
