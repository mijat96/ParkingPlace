package com.rmj.parking_place.actvities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.rmj.parking_place.R;

public class RegistrationActivity extends CheckWifiActivity /*AppCompatActivity*/ {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }}
