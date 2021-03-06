package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity implements JSONRequest.JSONListener {

    private EditText emailInput,
                     passwordInput;
    private Button registerButton;
    private JSONObject response = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Hide Top Bar
        this.getSupportActionBar().hide();

        this.emailInput = (EditText)findViewById(R.id.emailInput);
        this.passwordInput = (EditText)findViewById(R.id.passwordInput);
        this.registerButton = (Button)findViewById(R.id.loginButton);
    }

    public void returnToLogin(View v){
        Intent i = new Intent();

        finish();
    }

    public void register(View v){
        /*
            Result:
                OK: Correct
                ERROR: Incorrect
         */
        String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/registerOwner.php?email="+this.emailInput.getText().toString()+"&password="+this.passwordInput.getText().toString();

        new JSONRequest(this,this).execute(url);

    }

    @Override
    public void doSomething(JSONObject array) {
        try {
            //Get response value from CCAR Platform
            this.response = array;

            //Registered success
            if(!this.response.getString("userID").toString().equals("null")){
                //End this activity
                finish();

                //Go to Main Activity
                Intent i = new Intent(this, MainActivity.class);

                try {
                    i.putExtra("userID", response.getString("userID").toString());
                    i.putExtra("devices", response.getJSONArray("devices").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                startActivity(i);
            }else{
                //Error ocurred; Email taken
                Toast.makeText(this, "Ya existe ese email", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
