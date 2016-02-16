package com.example.andrestorresb.ccar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class LoginActivity extends AppCompatActivity implements JSONRequest.JSONListener{

    private EditText emailInput,
                     passwordInput;
    private Button loginButton;

    private Properties credentials;
    private String credentialsFile = "credentials.xml";
    private FileOutputStream fos;

    private String email = null;
    private String password = null;

    private boolean initCredentials = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Hide Top Bar
        this.getSupportActionBar().hide();

        this.emailInput = (EditText)findViewById(R.id.emailInput);
        this.passwordInput = (EditText)findViewById(R.id.passwordInput);
        this.loginButton = (Button)findViewById(R.id.loginButton);

        //Set Login Credentials
        this.setLoginCredentials();

    }

    private void setLoginCredentials(){
        File file = new File( getFilesDir(), this.credentialsFile );
        this.credentials = new Properties();

        try{
            if(file.exists()){
                //Load credentials if login success (previous)

                this.initCredentials = true;

                //Read file
                FileInputStream fis = openFileInput( this.credentialsFile );
                this.credentials.loadFromXML( fis );
                fis.close();

                //Read & set credentials
                this.email = this.credentials.getProperty("email");
                this.password = this.credentials.getProperty("password");

                //Auto login
                this.login(null);

                //Set output file
                this.fos = openFileOutput( this.credentialsFile, Context.MODE_PRIVATE );

                this.initCredentials = false;
            }else{
                //Create credentials for the first time

                //Create file
                this.fos = openFileOutput( this.credentialsFile, Context.MODE_PRIVATE );
                this.credentials.storeToXML(this.fos, null);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

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

        //Not First time app opened
        if(!this.initCredentials){
            this.email = this.emailInput.getText().toString();
            this.password = this.passwordInput.getText().toString();
        }

        //Validate credentials from CCAR Platform
        String url = "http://renatogutierrez.com/apps/CCAR/Plataforma/login.php?email=" + this.email + "&password=" + this.password;
        new JSONRequest(this,this).execute(url);

    }

    @Override
    public void doSomething(JSONObject array) {
        //Valid credentials
        try {
            JSONObject response = array;

            //If its valid user
            if(!response.getString("userID").toString().equals("null")){
                //Save credentials
                this.credentials.setProperty("email", this.email);
                this.credentials.setProperty("password", this.password);

                //Save to File
                this.credentials.storeToXML(fos, null);

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
                //Wrong credentials
                Toast.makeText(this, "Datos Incorrectos", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
