package com.rmj.parking_place.fragments;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.Zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FindParkingFragment extends Fragment {

    private View view;
    private MapFragment mapFragment;
    private MapPageFragment mapPageFragment;
    private String chosedSearchMethod;
    private com.google.android.gms.maps.model.LatLng clickedLocation;
    private List<Zone> zones;

    private double latitude;
    private  double longitude;

    public FindParkingFragment(MapFragment mapFragment, MapPageFragment mapPageFragment){
        this.mapFragment = mapFragment;
        this.mapPageFragment = mapPageFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view  = inflater.inflate(R.layout.find_parking_layout, container, false);

        Button findPlaceBtn = (Button) view.findViewById(R.id.closeSearch);
        findPlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickHideFindFragmentButton(v);
            }
        });

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

        Button search_parking_button = (Button) view.findViewById(R.id.search_parking_button);
        search_parking_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnBtnSearchParkingPlace(v);
            }
        });

        return view;
    }

    public void setTextToSelected(boolean selectedLocation){
        EditText editTextLocation = (EditText) view.findViewById(R.id.location_text_input);

        if(selectedLocation){
            editTextLocation.setText("not selected");
        }else{
            editTextLocation.setText("selected");
        }
    }

    public void onClickHideFindFragmentButton(View v){
        mapPageFragment.setInvisibilityOfMapPageFragmenView();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;

        ViewGroup.LayoutParams paramsMap = mapFragment.getView().getLayoutParams();
        paramsMap.height = height;
        mapFragment.getView().setLayoutParams(paramsMap);

        mapPageFragment.setVisibilityOfFindParkingButton();

    }

    public void clickOnBtnSearchParkingPlace(View v){
        EditText editTextAddress = (EditText) view.findViewById(R.id.address_text_input);
        String addressTextInput = editTextAddress.getText().toString();
        EditText editTextZone = (EditText) view.findViewById(R.id.zone_text_input);
        String zoneTextInput = editTextZone.getText().toString();
        EditText editTextLocation = (EditText) view.findViewById(R.id.location_text_input);
        EditText editTextDistance = (EditText) view.findViewById(R.id.location_distance_text_input);
        float distance = 0;
        if(!editTextDistance.getText().toString().matches("")){
            distance = Float.parseFloat(editTextDistance.getText().toString());
        }

        HashMap<Location, ParkingPlace> places = mapFragment.getParkingPlaces();
        ArrayList<ParkingPlace> parkingPlaces = new ArrayList<ParkingPlace>();
        zones = mapPageFragment.getZones();
        MainActivity mainActivity = (MainActivity) getActivity();
        for (ParkingPlace parkingPlace: places.values()) {
            if(!addressTextInput.matches("") && addressTextInput.equals(parkingPlace.getLocation().getAddress()) && chosedSearchMethod.matches("address")){
                Toast.makeText(mainActivity, "search address",Toast.LENGTH_SHORT).show();
                parkingPlaces.add(parkingPlace);
            } else if(!zoneTextInput.matches("") && zoneTextInput.equals(parkingPlace.getZone().getName()) && chosedSearchMethod.matches("zone")){
                Toast.makeText(mainActivity, "search address",Toast.LENGTH_SHORT).show();
                parkingPlaces.add(parkingPlace);
            } else if(!editTextLocation.getText().toString().matches("") && editTextLocation.getText().toString().matches("selected")
                    && chosedSearchMethod.matches("marker")){
                float distanceMarkerCurrentLocation = computeDistanceBetweenTwoPoints(latitude, longitude,
                        parkingPlace.getLocation().getLatitude(), parkingPlace.getLocation().getLongitude());
                if(distanceMarkerCurrentLocation <= distance*1000){
                    parkingPlaces.add(parkingPlace);
                }
                //Toast.makeText(this, "search location",Toast.LENGTH_SHORT).show();
            } else {
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
        EditText editTextMarker = (EditText) view.findViewById(R.id.location_text_input);
        EditText editTextlocationDistance = (EditText) view.findViewById(R.id.location_distance_text_input);

        switch(v.getId()) {
            case R.id.addressCheckBox:
                if (checked){
                    chosedSearchMethod = "address";
                    markerCheckBox.setChecked(false);
                    zoneCheckBox.setChecked(false);
                    editTextZone.setFocusable(false);
                    editTextMarker.setFocusable(false);
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
                    editTextMarker.setFocusable(false);
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
                    editTextlocationDistance.setFocusable(true);
                    editTextlocationDistance.setFocusableInTouchMode(true);
                    editTextlocationDistance.requestFocus();
                }
                else{
                    chosedSearchMethod = "";
                    editTextMarker.setFocusable(false);
                    editTextlocationDistance.setFocusable(false);
                }
                break;
        }
    }

    public void setClickedLocation(com.google.android.gms.maps.model.LatLng latLng){
        clickedLocation = latLng;
        latitude = latLng.latitude;
        longitude = latLng.longitude;
        EditText editTextLocation = (EditText) view.findViewById(R.id.location_text_input);
        editTextLocation.setText("selected");
    }


}
