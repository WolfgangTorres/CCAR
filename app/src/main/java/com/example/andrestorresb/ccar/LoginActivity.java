package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput,
                     passwordInput;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Hide Top Bar
        this.getSupportActionBar().hide();

        this.emailInput = (EditText)findViewById(R.id.emailInput);
        this.passwordInput = (EditText)findViewById(R.id.passwordInput);
        this.loginButton = (Button)findViewById(R.id.loginButton);

    }

    public void goToRegister(View v){
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    public void login(View v) {
        //Request CCAR Platform to login
        /*
            Result:
                Correct
                {
                    "userID":"-KASHqKGIjEhpt8w4NLh",
                    "devices":[]
                }
                null: Incorrect
         */

        //Validate credentials from CCAR Platform
        String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/login.php?email=" + this.emailInput.getText().toString() + "&password=" + this.passwordInput.getText().toString();

        String response = null;

        //Valid credentials
        if(response != null){
            //End this activity
            finish();

            //Go to Main Activity
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }else{
            //Wrong credentials
            Toast.makeText(this, "Datos Incorrectos", Toast.LENGTH_SHORT).show();
        }


    }
}
