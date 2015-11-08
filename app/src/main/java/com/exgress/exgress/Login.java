package com.exgress.exgress;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
    EditText username;
    EditText password;

    private class LoginUserModel {
        String username;
        String password;

        public LoginUserModel(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        username = (EditText) findViewById(R.id.editText);
        password = (EditText) findViewById(R.id.editText2);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void loginpro(View v) {
        LoginUserModel loginUserModel = new LoginUserModel(
                username.getText().toString(),
                password.getText().toString());
        new GetLoginTask().execute(loginUserModel);
    }

    public void registerpro(View v) {
        Intent intentreg = new Intent(this, Register.class);
        startActivity(intentreg);
    }

    private class GetLoginTask extends AsyncTask<LoginUserModel, Void, Void> {

        @Override
        protected Void doInBackground(LoginUserModel... params) {
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
                        "  \"username\": \"" + params[0].username + "\",\n" +
                        "  \"password\": \""+ params[0].password + "\"\n" +
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

                final JSONObject jResult = new JSONObject(result);
                String re = jResult.getString("Response");
                if(re.equals("success")) {
                    Intent intentlog = new Intent(getApplicationContext(), World.class);
                    intentlog.putExtra("faction", jResult.getString(Constants.FactionColumn));
                    startActivity(intentlog);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(Login.this, jResult.getString("Response"), Toast.LENGTH_LONG).show();
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

