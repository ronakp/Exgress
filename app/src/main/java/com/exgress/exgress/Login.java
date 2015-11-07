package com.exgress.exgress;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {

    InputStream in = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

    public void loginpro(View v) {

        try {
            URL url = new URL("");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type","application/json");
            
            in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (Exception e ) {
            System.out.println(e.getMessage());
        }
        Intent intent1 = new Intent(this, Profile.class);
        startActivity(intent1);
    }

    public void registerpro(View v) {
        Intent intent2 = new Intent(this, Register.class);
        startActivity(intent2);
    }
}

