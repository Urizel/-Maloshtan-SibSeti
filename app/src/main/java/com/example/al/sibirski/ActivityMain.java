package com.example.al.sibirski;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

// XXX redundant activities - can be replaces with one parametrized
public class ActivityMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void callProvider(View v) {
        IntentDialer.callProvider(this);
    }

}
