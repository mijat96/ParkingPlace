package com.rmj.parking_place.actvities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.rmj.parking_place.R;
import com.rmj.parking_place.model.FavoritePlace;
import com.rmj.parking_place.model.FavoritePlaceType;
import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.model.TicketType;
import com.rmj.parking_place.utils.GeocoderUtils;

import java.util.ArrayList;

public class AddOrEditFavoritePlaceActivity extends AppCompatActivity {

    private static final int PICK_MAP_POINT_REQUEST = 999;  // The request code

    private Location selectedLocation = null;
    private static String[]  favoritePlaceTypes =  {
        FavoritePlaceType.HOME.name(),
        FavoritePlaceType.WORK.name(),
        FavoritePlaceType.OTHER.name()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_favorite_place);

        Intent intent = getIntent();

        if (savedInstanceState == null) {
            selectedLocation = (Location) intent.getParcelableExtra("selected_location");
        }
        else {
            selectedLocation = savedInstanceState.getParcelable("selectedLocation");
        }

        if (selectedLocation != null) {
            String address;
            if (selectedLocation.getAddress() == null) {
                address = GeocoderUtils.getAddressFromLatLng(new LatLng(selectedLocation.getLatitude(), selectedLocation.getLongitude()));
                selectedLocation.setAddress(address);
            }
            else {
                address = selectedLocation.getAddress();
            }

            TextView selectedLocationTxt = findViewById(R.id.selectedLocationTxt);
            selectedLocationTxt.setText(address);
        }

        String favoritePlaceName = intent.getStringExtra("favorite_place_name");
        if (favoritePlaceName != null && !favoritePlaceName.equals("")) {
            EditText nameEditText = findViewById(R.id.favoritePlaceName);
            nameEditText.setText(favoritePlaceName);
        }

        Spinner spinner = (Spinner) findViewById(R.id.favoritePlaceType);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter =
                new ArrayAdapter<CharSequence>(this,
                        R.layout.support_simple_spinner_dropdown_item, android.R.id.text1, favoritePlaceTypes);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        String favoritePlaceTypeStr = intent.getStringExtra("favoritePlaceType");
        if (favoritePlaceTypeStr != null && !favoritePlaceTypeStr.equals("")) {
            int position = getPositionOfFavoritePlaceTypes(favoritePlaceTypeStr);
            if (position == -1) {
                position = 0;
            }
            spinner.setSelection(position, true);
        }

        initActionBar();
    }

    private void initActionBar() {
        // Toolbar toolbar = findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.appBarColor));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable("selectedLocation", selectedLocation);

        super.onSaveInstanceState(outState);
    }

    private static int getPositionOfFavoritePlaceTypes(String favoritePlaceType) {
        for (int i = 0; i < favoritePlaceTypes.length; i++) {
            if (favoritePlaceTypes[i].equals(favoritePlaceType)) {
                return i;
            }
        }
        return -1;
    }

    public void clickOnBtnSelectLocation(View view) {
        pickPointOnMap();
    }

    private void pickPointOnMap() {
        Intent pickPointIntent = new Intent(this, SelectLocationActivity.class);
        if (selectedLocation != null) {
            pickPointIntent.putExtra("picked_point",
                    new LatLng(selectedLocation.getLatitude(), selectedLocation.getLongitude()));
        }
        startActivityForResult(pickPointIntent, PICK_MAP_POINT_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MAP_POINT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                LatLng latLng = (LatLng) data.getParcelableExtra("picked_point");
                String address = GeocoderUtils.getAddressFromLatLng(latLng);
                selectedLocation = new Location(latLng.latitude, latLng.longitude, address);
                TextView selectedLocationTxt = findViewById(R.id.selectedLocationTxt);
                selectedLocationTxt.setText(address);
            }
        }
    }

    public void clickOnBtnSave(View view) {
        //Toast.makeText(this, "CAOOOOOOOOOOO", Toast.LENGTH_SHORT).show();

        EditText nameEditText = findViewById(R.id.favoritePlaceName);
        String name = nameEditText.getText().toString().trim();
        if (name.equals("")) {
            Toast.makeText(this, "name.equals(\"\")", Toast.LENGTH_SHORT).show();
            return;
        }

        Spinner spinner = (Spinner) findViewById(R.id.favoritePlaceType);
        String favoritePlaceTypeStr = (String) spinner.getSelectedItem();
        if (favoritePlaceTypeStr == null) {
            Toast.makeText(this, "favoritePlaceTypeStr == null", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLocation == null) {
            Toast.makeText(this, "selectedLocation == null", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra("favorite_place_name", name);
        returnIntent.putExtra("favoritePlaceType", favoritePlaceTypeStr);
        returnIntent.putExtra("selected_location", selectedLocation);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}
