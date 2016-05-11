package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);


        TimerTask tt=new TimerTask() {
            @Override
            public void run() {
                Intent i=new Intent(SplashScreen.this,LoginActivity.class);
                startActivity(i);
                finish();
            }
        };
        Timer time=new Timer();
        time.schedule(tt,3000);
    }
}
