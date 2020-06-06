package com.rmj.parking_place.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.ReportIlegalyParkedActivity;
import com.rmj.parking_place.model.ParkingPlace;

public class ParkingPlaceInfoFragment extends Fragment {

    private View view;
    private MapFragment mapFragment;

    public static ParkingPlaceInfoFragment newInstance() {

        ParkingPlaceInfoFragment pif = new ParkingPlaceInfoFragment();

        return pif;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view  = inflater.inflate(R.layout.parking_place_info_layout, container, false);

        if (savedInstanceState != null) {
            TextView textStatus = (TextView) view.findViewById(R.id.status);
            TextView textAddress = (TextView) view.findViewById(R.id.address);
            TextView textZone = (TextView) view.findViewById(R.id.zone);
            TextView textDistance = (TextView) view.findViewById(R.id.distance);
            TextView textRealDistance = (TextView) view.findViewById(R.id.realDistance);

            textStatus.setText(savedInstanceState.getString("textStatus"));
            textAddress.setText(savedInstanceState.getString("textAddress"));
            textZone.setText(savedInstanceState.getString("textZone"));
            textDistance.setText(savedInstanceState.getString("textDistance"));
            textRealDistance.setText(savedInstanceState.getString("textRealDistance"));
        }

        Button search_parking_button = (Button) view.findViewById(R.id.view_report_parking_fragment_button);
        search_parking_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnBtnShowReportView(v);
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        TextView textStatus = (TextView) view.findViewById(R.id.status);
        TextView textAddress = (TextView) view.findViewById(R.id.address);
        TextView textZone = (TextView) view.findViewById(R.id.zone);
        TextView textDistance = (TextView) view.findViewById(R.id.distance);
        TextView textRealDistance = (TextView) view.findViewById(R.id.realDistance);

        outState.putString("textStatus", textStatus.getText().toString());
        outState.putString("textAddress", textAddress.getText().toString());
        outState.putString("textZone", textZone.getText().toString());
        outState.putString("textDistance", textDistance.getText().toString());
        outState.putString("textRealDistance", textRealDistance.getText().toString());

        super.onSaveInstanceState(outState);
    }

    public void setRealDistance(String realDistanceText) {
        TextView textRealDistance = (TextView) view.findViewById(R.id.realDistance);
        textRealDistance.setText(realDistanceText);
    }

    public void setData(ParkingPlace selectedParkingPlace, float distance) {
        TextView textStatus = (TextView) view.findViewById(R.id.status);
        textStatus.setText("Status: " + selectedParkingPlace.getStatus().toString());

        TextView textAddress = (TextView) view.findViewById(R.id.address);
        textAddress.setText("Address: " + selectedParkingPlace.getLocation().getAddress());

        TextView textZone = (TextView) view.findViewById(R.id.zone);
        textZone.setText("Zone: " + selectedParkingPlace.getZone().getName());

        float distanceKm = distance / 1000;
        TextView textDistance = (TextView) view.findViewById(R.id.distance);
        textDistance.setText("Distance: " + distanceKm + "km");
    }

    public void setMapFragment(MapFragment mapFragment){
        this.mapFragment = mapFragment;
    }

    public void clickOnBtnShowReportView(View v){
        Intent intent = new Intent(getContext(), ReportIlegalyParkedActivity.class);
        intent.putExtra("selected_parking_place", mapFragment.getSelectedParkingPlace());
        startActivity(intent);
    }
}
