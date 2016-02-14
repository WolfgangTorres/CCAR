package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput,
                     passwordInput;
    private Button registerButton;

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
                otherwise: Incorrect
         */
        String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/registerOwner.php?devID=1&email="+this.emailInput.getText().toString()+"&password="+this.passwordInput.getText().toString();

        String response = "OK";

        if(response == "OK"){
            //End this activity
            finish();

            //Go to Main Activity
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }else{
            //Error ocurred
            Toast.makeText(this, "Hubo un error :(", Toast.LENGTH_SHORT).show();
        }
    }
}
