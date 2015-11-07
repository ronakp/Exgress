package com.exgress.exgress;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {

    InputStream in = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        new GetLoginTask().execute();
    }

    public void loginpro(View v) {


        Intent intent1 = new Intent(this, Profile.class);
        startActivity(intent1);
    }

    public void registerpro(View v) {
        Intent intent2 = new Intent(this, Register.class);
        startActivity(intent2);
    }

    private class GetLoginTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://exgress.azurewebsites.net/api/Account/LogIn");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type","application/JSON");
                urlConnection.connect();
                //Write
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write("{ \n" +
                        "  \"username\": \"tester\",\n" +
                        "  \"password\": \"password\"\n" +
                        "}");
                writer.close();
                os.close();
                //Read
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                String result = sb.toString();
                Toast.makeText(Login.this, result, Toast.LENGTH_SHORT).show();
            /*in = new BufferedInputStream(urlConnection.getInputStream());*/
            } catch (Exception e ) {

                System.out.println(e.getMessage());
            }
            return null;
        }
    }
}

