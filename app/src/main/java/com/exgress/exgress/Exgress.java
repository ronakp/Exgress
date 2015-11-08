package com.exgress.exgress;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Exgress extends Activity {

    View mainView;
    MapInfoFragment mapInfoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exgress);
        mainView = findViewById(R.id.ExgressMain);

        mapInfoFragment = (MapInfoFragment) getFragmentManager().findFragmentById(R.id.ExgressMapInfoFragment);


    }
}
