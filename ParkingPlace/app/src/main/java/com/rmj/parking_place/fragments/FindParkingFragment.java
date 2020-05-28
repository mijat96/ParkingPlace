package com.rmj.parking_place.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.model.LatLng;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.actvities.SelectLocationActivity;
import com.rmj.parking_place.listener.OnCreateViewFinishedListener;
import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.Zone;
import com.rmj.parking_place.utils.GeocoderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FindParkingFragment extends Fragment {

    private static final int PICK_MAP_POINT_REQUEST = 999;  // The request code

    private View view;
    private String chosedSearchMethod;
    private com.google.android.gms.maps.model.LatLng clickedLocation;
    private List<Zone> zones;

    private double latitude;
    private  double longitude;

    private MapPageFragment mapPageFragment;
    private MainActivity mainActivity;

    private OnCreateViewFinishedListener onCreateViewFinishedListener;


    public FindParkingFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapPageFragment = (MapPageFragment) getParentFragment();

        mainActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view  = inflater.inflate(R.layout.find_parking_layout, container, false);

        if (savedInstanceState != null) {
            CheckBox addressCheckBox = (CheckBox) view.findViewById(R.id.addressCheckBox);
            EditText addressTextInput = (EditText) view.findViewById(R.id.address_text_input);
            CheckBox zoneCheckBox = (CheckBox) view.findViewById(R.id.zoneCheckBox);
            EditText zoneTextInput = (EditText) view.findViewById(R.id.zone_text_input);
            CheckBox markerCheckBox = (CheckBox) view.findViewById(R.id.markerCheckBox);
            TextView locationText = (TextView) view.findViewById(R.id.location_text);
            EditText locationDistanceTextInput = (EditText) view.findViewById(R.id.location_distance_text_input);

            addressCheckBox.setChecked(savedInstanceState.getBoolean("addressChecked"));
            addressTextInput.setText(savedInstanceState.getString("addressText"));
            addressTextInput.setFocusable(savedInstanceState.getBoolean("addressTextFocusable"));

            zoneCheckBox.setChecked(savedInstanceState.getBoolean("zoneChecked"));
            zoneTextInput.setText(savedInstanceState.getString("zoneText"));
            zoneTextInput.setFocusable(savedInstanceState.getBoolean("zoneTextFocusable"));

            markerCheckBox.setChecked(savedInstanceState.getBoolean("markerChecked"));
            locationText.setText(savedInstanceState.getString("locationText"));
            locationDistanceTextInput.setText(savedInstanceState.getString("locationDistanceText"));
            locationDistanceTextInput.setFocusable(savedInstanceState.getBoolean("locationDistanceTextFocusable"));

            chosedSearchMethod = savedInstanceState.getString("chosedSearchMethod");
            clickedLocation = savedInstanceState.getParcelable("clickedLocation");
        }

        /*Button closeSearchBtn = (Button) view.findViewById(R.id.closeSearch);
        closeSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickHideFindFragmentButton(v);
            }
        });*/

        CheckBox addressCheckBox = (CheckBox) view.findViewById(R.id.addressCheckBox);
        addressCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckboxSearchClicked(v);
            }
        });

        CheckBox zoneCheckBox = (CheckBox) view.findViewById(R.id.zoneCheckBox);
        zoneCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckboxSearchClicked(v);
            }
        });

        CheckBox markerCheckBox = (CheckBox) view.findViewById(R.id.markerCheckBox);
        markerCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckboxSearchClicked(v);
            }
        });

        Button selectLocationBtn = (Button) view.findViewById(R.id.selectLocationBtn);
        selectLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               pickPointOnMap();
            }
        });

        Button search_parking_button = (Button) view.findViewById(R.id.search_parking_button);
        search_parking_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnBtnSearchParkingPlace(v);
            }
        });

        if (savedInstanceState != null && onCreateViewFinishedListener != null) {
            onCreateViewFinishedListener.OnCreateViewFinished();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        CheckBox addressCheckBox = (CheckBox) view.findViewById(R.id.addressCheckBox);
        EditText addressTextInput = (EditText) view.findViewById(R.id.address_text_input);
        CheckBox zoneCheckBox = (CheckBox) view.findViewById(R.id.zoneCheckBox);
        EditText zoneTextInput = (EditText) view.findViewById(R.id.zone_text_input);
        CheckBox markerCheckBox = (CheckBox) view.findViewById(R.id.markerCheckBox);
        TextView locationText = (TextView) view.findViewById(R.id.location_text);
        EditText locationDistanceTextInput = (EditText) view.findViewById(R.id.location_distance_text_input);

        outState.putBoolean("addressChecked", addressCheckBox.isChecked());
        outState.putString("addressText", addressTextInput.getText().toString());
        outState.putBoolean("addressTextFocusable", addressTextInput.isFocusable());

        outState.putBoolean("zoneChecked", zoneCheckBox.isChecked());
        outState.putString("zoneText", zoneTextInput.getText().toString());
        outState.putBoolean("zoneTextFocusable", zoneTextInput.isFocusable());

        outState.putBoolean("markerChecked", markerCheckBox.isChecked());
        outState.putString("locationText", locationText.getText().toString());
        outState.putString("locationDistanceText", locationDistanceTextInput.getText().toString());
        outState.putBoolean("locationDistanceTextFocusable", locationDistanceTextInput.isFocusable());

        outState.putString("chosedSearchMethod", chosedSearchMethod);
        outState.putParcelable("clickedLocation", clickedLocation);

        super.onSaveInstanceState(outState);
    }

    private void pickPointOnMap() {
        Intent pickPointIntent = new Intent(mainActivity, SelectLocationActivity.class);
        if (clickedLocation != null) {
            pickPointIntent.putExtra("picked_point", clickedLocation);
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
                setClickedLocation(latLng);
                Toast.makeText(mainActivity, "Point Chosen: (" + latLng.latitude + ", "
                        + latLng.longitude + ")", Toast.LENGTH_LONG).show();
            }
        }
    }

    /*public void setTextToSelected(boolean selectedLocation){
        TextView textLocation = (TextView) view.findViewById(R.id.location_text);

        if(selectedLocation){
            textLocation.setText("not selected");
        }else{
            textLocation.setText("selected");
        }
    }*/

    /*public void onClickHideFindFragmentButton(View v) {
//        mapPageFragment.setInvisibilityOfMapPageFragmenView();
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        int height = displaymetrics.heightPixels;
//
//        ViewGroup.LayoutParams paramsMap = mapFragment.getView().getLayoutParams();
//        paramsMap.height = height;
//        mapFragment.getView().setLayoutParams(paramsMap);

        mapPageFragment.returnGoogleLogoOnStartPosition();
        mapPageFragment.setVisibilityOfFindParkingButton();
        mapPageFragment.setInvisibilityOfFindParkingFragment();
    }*/

    public void clickOnBtnSearchParkingPlace(View v){
        EditText editTextAddress = (EditText) view.findViewById(R.id.address_text_input);
        String addressTextInput = editTextAddress.getText().toString();
        EditText editTextZone = (EditText) view.findViewById(R.id.zone_text_input);
        String zoneTextInput = editTextZone.getText().toString();
        TextView textLocation = (TextView) view.findViewById(R.id.location_text);
        EditText editTextDistance = (EditText) view.findViewById(R.id.location_distance_text_input);
        float distance = 0;
        if(!editTextDistance.getText().toString().matches("")){
            distance = Float.parseFloat(editTextDistance.getText().toString());
        }

        HashMap<Location, ParkingPlace> places = mapPageFragment.getParkingPlaces();
        ArrayList<ParkingPlace> parkingPlaces = new ArrayList<ParkingPlace>();
        zones = mapPageFragment.getZones();

        for (ParkingPlace parkingPlace: places.values()) {
            if(!addressTextInput.matches("") && addressTextInput.equals(parkingPlace.getLocation().getAddress()) && chosedSearchMethod.matches("address")){
                Toast.makeText(mainActivity, "search address",Toast.LENGTH_SHORT).show();
                parkingPlaces.add(parkingPlace);
            }
            else if(!zoneTextInput.matches("") && zoneTextInput.equals(parkingPlace.getZone().getName()) && chosedSearchMethod.matches("zone")){
                Toast.makeText(mainActivity, "search address",Toast.LENGTH_SHORT).show();
                parkingPlaces.add(parkingPlace);
            }
            /*else if(!textLocation.getText().toString().matches("") && textLocation.getText().toString().matches("selected")
                    && chosedSearchMethod.matches("marker")) {*/
            else if(clickedLocation != null && chosedSearchMethod.matches("marker")) {
                float distanceMarkerCurrentLocation = computeDistanceBetweenTwoPoints(latitude, longitude,
                        parkingPlace.getLocation().getLatitude(), parkingPlace.getLocation().getLongitude());
                if(distanceMarkerCurrentLocation <= distance*1000){
                    parkingPlaces.add(parkingPlace);
                }
                //Toast.makeText(this, "search location",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(mainActivity, "select search method and fill in the field",Toast.LENGTH_SHORT).show();
            }
        }

        if (!parkingPlaces.isEmpty()) {
            FragmentManager fm = getFragmentManager();
            MapFragment fragm = (MapFragment) fm.findFragmentById(R.id.mapContent);
            double latitude = parkingPlaces.get(0).getLocation().getLatitude();
            double longitude = parkingPlaces.get(0).getLocation().getLongitude();
            com.google.android.gms.maps.model.LatLng lng = new com.google.android.gms.maps.model.LatLng(latitude, longitude);
            fragm.updateCameraPosition(lng, true);
        }
    }

    float computeDistanceBetweenTwoPoints(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(latitudeA, longitudeA, latitudeB, longitudeB, results);
        return results[0];
    }

    public void onCheckboxSearchClicked(View v){
        boolean checked = ((CheckBox) v).isChecked();
        CheckBox markerCheckBox =(CheckBox) view.findViewById(R.id.markerCheckBox);
        CheckBox zoneCheckBox =(CheckBox) view.findViewById(R.id.zoneCheckBox);
        CheckBox addressCheckBox =(CheckBox) view.findViewById(R.id.addressCheckBox);
        EditText editTextAddress = (EditText) view.findViewById(R.id.address_text_input);
        EditText editTextZone = (EditText) view.findViewById(R.id.zone_text_input);
        TextView textMarker = (TextView) view.findViewById(R.id.location_text);
        Button selectLocationBtn = (Button) view.findViewById(R.id.selectLocationBtn);
        EditText editTextlocationDistance = (EditText) view.findViewById(R.id.location_distance_text_input);

        switch(v.getId()) {
            case R.id.addressCheckBox:
                if (checked){
                    chosedSearchMethod = "address";
                    markerCheckBox.setChecked(false);
                    zoneCheckBox.setChecked(false);
                    editTextZone.setFocusable(false);
                    textMarker.setFocusable(false);
                    selectLocationBtn.setEnabled(false);
                    editTextlocationDistance.setFocusable(false);
                    editTextAddress.setFocusable(true);
                    editTextAddress.setFocusableInTouchMode(true);
                    editTextAddress.requestFocus();
                }
                else{
                    chosedSearchMethod = "";
                    editTextAddress.setFocusable(false);
                }
                break;
            case R.id.zoneCheckBox:
                if (checked){
                    chosedSearchMethod = "zone";
                    markerCheckBox.setChecked(false);
                    addressCheckBox.setChecked(false);
                    editTextAddress.setFocusable(false);
                    textMarker.setFocusable(false);
                    selectLocationBtn.setEnabled(false);
                    editTextlocationDistance.setFocusable(false);
                    editTextZone.setFocusable(true);
                    editTextZone.setFocusableInTouchMode(true);
                    editTextZone.requestFocus();
                }
                else{
                    chosedSearchMethod = "";
                    editTextZone.setFocusable(false);
                }
                break;
            case R.id.markerCheckBox:
                if (checked){
                    chosedSearchMethod = "marker";
                    addressCheckBox.setChecked(false);
                    zoneCheckBox.setChecked(false);
                    editTextZone.setFocusable(false);
                    editTextAddress.setFocusable(false);
                    selectLocationBtn.setEnabled(true);
                    editTextlocationDistance.setFocusable(true);
                    editTextlocationDistance.setFocusableInTouchMode(true);
                    editTextlocationDistance.requestFocus();
                }
                else{
                    chosedSearchMethod = "";
                    textMarker.setFocusable(false);
                    editTextlocationDistance.setFocusable(false);
                }
                break;
        }
    }

    public void setClickedLocation(LatLng latLng){
        clickedLocation = latLng;
        latitude = latLng.latitude;
        longitude = latLng.longitude;
        TextView locationText = view.findViewById(R.id.location_text);
        String fullAddress = GeocoderUtils.getAddressFromLatLng(latLng);
        String address = fullAddress.split(",")[0];
        locationText.setText(address);
        // locationText.setTooltipText(fullAddress);
    }


    public int getHeight() {
        //int height = view.getHeight();
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int height = view.getMeasuredHeight();
        // na ovaj nacin uzimamo height zato sto kad se prvi put menja visibility sa GONE na VISIBLE,
        // moze se desiti da ocitamo vrednost fragmenta (view) pre nego sto se on resize
        // i onda dobijamo height = 0
        return height;
    }

    public void setOnCreateViewFinishedListener(OnCreateViewFinishedListener onCreateViewFinishedListener) {
        this.onCreateViewFinishedListener = onCreateViewFinishedListener;
    }
}
