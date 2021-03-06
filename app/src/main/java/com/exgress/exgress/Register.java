package com.exgress.exgress;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Register extends AppCompatActivity {
    EditText username;
    EditText email;
    EditText password;
    EditText repassword;
    private class NewUserModel {
        String username;
        String password;
        String email;

        public NewUserModel(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        username = (EditText) findViewById(R.id.editText3);
        email = (EditText) findViewById(R.id.editText4);
        password = (EditText) findViewById(R.id.editText5);
        repassword = (EditText) findViewById(R.id.editText6);
    }
    public void register(View v) {
        NewUserModel newUserModel = new NewUserModel(
                username.getText().toString(),
                password.getText().toString(),
                email.getText().toString());
        if(password.getText().toString().equals(repassword.getText().toString()))
        {
            new GetRegisterTask().execute(newUserModel);
        }
        else
        {
            Toast.makeText(Register.this, "Password Does't Match", Toast.LENGTH_LONG).show();
        }
    }

    private class GetRegisterTask extends AsyncTask<NewUserModel, Void, Void> {

        @Override
        protected Void doInBackground(NewUserModel... params) {
            try {
                URL url = new URL("http://exgress.azurewebsites.net/api/Account/CreateUser");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type","application/JSON");
                urlConnection.connect();
                //Write
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write("{\n" +
                        "  \"Username\": \"" + params[0].username + "\",\n" +
                        "  \"Password\": \"" + params[0].password + "\",\n" +
                        "  \"Email\": \"" + params[0].email + "\"\n" +
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
                String resultr = sb.toString();
                final JSONObject jResultl = new JSONObject(resultr);
                String ree = jResultl.getString("Response");
                if(ree.equals("success")) {
                    Intent intentlog = new Intent(getApplicationContext(), Login.class);
                    startActivity(intentlog);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(Register.this, jResultl.getString("Response"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e ) {
                System.out.println(e.getMessage());
            }
            return null;
        }
    }
}
