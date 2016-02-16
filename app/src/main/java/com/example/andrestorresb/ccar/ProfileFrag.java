package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

/**
 * Created by andrestorres on 2/15/16.
 */
public class ProfileFrag extends AppCompatActivity implements JSONRequest.JSONListener{
    EditText nombre,apellido;
    Button guardar;
    Intent i;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_profile);

        //Hide Top Bar
        this.getSupportActionBar().setTitle("Editar Usuario");

        this.nombre=(EditText)findViewById(R.id.cambiarNombre);
        this.apellido=(EditText)findViewById(R.id.cambiarApellido);
        this.guardar=(Button)findViewById(R.id.button);
        this.i=getIntent();
    }

    public void guardarDatos(View v){
        new JSONRequest(this,this)
                .execute("http://renatogutierrez.com/apps/CCAR/Plataforma/updateUserPersonalInfo.php?userID="+this.i.getStringExtra("userID")
                        + "&name=" + this.nombre.getText() + "&lastname=" + this.apellido.getText());
        finish();

    }


    @Override
    public void doSomething(JSONObject array) {
        if(array!=null){
            Toast.makeText(this, "Se Modifico Satisfactoriamente", Toast.LENGTH_SHORT).show();
        }
    }
}
