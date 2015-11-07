package com.exgress.exgress;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;



public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

    public void loginpro(View v) {
        Intent intent1 = new Intent(this, Profile.class);
        startActivity(intent1);
    }
    public void registerpro(View v) {
        Intent intent2 = new Intent(this, Register.class);
        startActivity(intent2);
    }
}

