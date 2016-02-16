package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andrestorres on 2/15/16.
 */
public class ProfileFrag extends AppCompatActivity implements JSONRequest.JSONListener{
    private EditText nameInput, lastnameInput;
    private Button save;
    private Intent i;

    private String userID;
    private String userName;
    private String userLastname;

    private int actionFirebase;

    private final int GET_USER_PERSONAL_INFO = 0,
                      SAVE_USER_PERSONAL_INFO = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_profile);

        //Hide Top Bar
        this.getSupportActionBar().setTitle("Editar Usuario");

        this.nameInput = (EditText)findViewById(R.id.cambiarNombre);
        this.lastnameInput = (EditText)findViewById(R.id.cambiarApellido);
        this.save = (Button)findViewById(R.id.button);

        Intent i = getIntent();
        this.userID = i.getStringExtra("userID");

        //Get user personal info from CCAR Platform
        this.getUserInfo();
    }

    private void getUserInfo(){
        this.actionFirebase = GET_USER_PERSONAL_INFO;

        new JSONRequest(this, this)
                .execute("http://renatogutierrez.com/apps/CCAR/Plataforma/getUserPersonalInfo.php?userID=" + this.userID);
    }

    public void guardarDatos(View v){
        this.actionFirebase = SAVE_USER_PERSONAL_INFO;

        this.userName = this.nameInput.getText().toString();
        this.userLastname = this.lastnameInput.getText().toString();

        new JSONRequest(this,this)
                .execute("http://renatogutierrez.com/apps/CCAR/Plataforma/updateUserPersonalInfo.php?userID=" + this.userID
                        + "&name=" + this.userName + "&lastname=" + this.userLastname);
        finish();

    }


    @Override
    public void doSomething(JSONObject array) {

        switch (this.actionFirebase){
            case GET_USER_PERSONAL_INFO:

                try {
                    if(array.getString("response") != "null") {
                        this.userName = array.getJSONObject("response").getString("name");
                        this.userLastname = array.getJSONObject("response").getString("lastname");

                        this.nameInput.setText(this.userName);
                        this.lastnameInput.setText(this.userLastname);
                    }else{
                        Toast.makeText(this, "Bad auth", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;

            case SAVE_USER_PERSONAL_INFO:

                try {
                    if(array.getString("response") != "null"){
                        Toast.makeText(this, "Se Modifico Satisfactoriamente", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }
    }
}
