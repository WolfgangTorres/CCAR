package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import java.util.ArrayList;

public class Editar extends AppCompatActivity {

    EditText modelo,color,placas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        this.modelo=(EditText)findViewById(R.id.editModelo);
        this.color=(EditText)findViewById(R.id.editColor);
        this.placas=(EditText)findViewById(R.id.editPlacas);

        Intent i=getIntent();
        ArrayList array;
        array=i.getStringArrayListExtra("carro");
        this.modelo.setText(array.get(0).toString());
        this.color.setText(array.get(1).toString());
        this.placas.setText(array.get(2).toString());
    }
}
