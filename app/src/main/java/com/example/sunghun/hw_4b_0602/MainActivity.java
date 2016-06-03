package com.example.sunghun.hw_4b_0602;

import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(getApplicationContext(),Hw4B.class);
        startActivity(i);
    }
}
