package com.rmj.parking_place.actvities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.rmj.parking_place.R;

public class SelectLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private com.androidmapsextensions.GoogleMap map;
    private LatLng startPosition;

    private com.androidmapsextensions.SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            startPosition = intent.getParcelableExtra("picked_point");
            mapFragment = (com.androidmapsextensions.SupportMapFragment) fm.findFragmentById(R.id.mapFragment);
            mapFragment.setRetainInstance(true);
            mapFragment.getExtendedMapAsync(this);
        }
        else {
            mapFragment = (com.androidmapsextensions.SupportMapFragment) fm.getFragment(savedInstanceState, "mapFragment");
            map = mapFragment.getExtendedMap();
        }

        initActionBar();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        FragmentManager fm = getSupportFragmentManager();
        fm.putFragment(outState, "mapFragment", mapFragment);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (startPosition == null) {
            startPosition = new LatLng(45.253335,19.844794);
        }
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(startPosition).zoom(15).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.moveCamera(cameraUpdate);
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.select_location_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btnConfirm) {
            clickOnBtnConfirm(item);
        }
        else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionBar() {
        // Toolbar toolbar = findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.appBarColor));
        }
    }

    public void clickOnBtnConfirm(MenuItem item) {
        //Toast.makeText(this, "CAOOOOOOOOOOO", Toast.LENGTH_SHORT).show();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("picked_point", map.getCameraPosition().target);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}
