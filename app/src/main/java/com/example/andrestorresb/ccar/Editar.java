package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Editar extends ActionBarActivity implements JSONRequest.JSONListener {

    EditText brandInput,colorInput,placas, modelInput;
    Button boton;

    private static String OK = "OK";
    private String deviceID;
    private String brand,
                model,
                color,
                plates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.brandInput=(EditText)findViewById(R.id.editBrand);
        this.colorInput=(EditText)findViewById(R.id.editColor);
        this.placas=(EditText)findViewById(R.id.editPlacas);
        this.modelInput = (EditText)findViewById(R.id.editModel);
        this.boton=(Button)findViewById(R.id.btnGuardar);

        Intent i=getIntent();
        ArrayList array;
        array=i.getStringArrayListExtra("carro");
        this.brandInput.setText(array.get(0).toString());
        this.colorInput.setText(array.get(1).toString());
        this.placas.setText(array.get(2).toString());
        this.modelInput.setText(array.get(3).toString());

        this.deviceID = i.getStringExtra("deviceID");
    }

    public void guardar(View v){
        this.brand = this.brandInput.getText().toString();
        this.plates = this.placas.getText().toString();
        this.color = this.colorInput.getText().toString();
        this.model = this.modelInput.getText().toString();

        String endpoint = "http://renatogutierrez.com/apps/CCAR/Plataforma/updateCarInfo.php?devID=" + this.deviceID + "&cbrand=" + this.brand + "&ccolor=" + this.color + "&cplates=" + this.plates + "&cmodel=" + this.model;

        new JSONRequest(this, this).execute(endpoint);

        finish();
    }

    @Override
    public void doSomething(JSONObject array) {
        if(array != null){
            try{
                String status = array.getString("response");

                if(status.equals(OK)){
                    Toast.makeText(this,"Se actualizo exitosamente",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }
}
