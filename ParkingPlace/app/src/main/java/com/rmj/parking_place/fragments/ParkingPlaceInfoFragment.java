package com.rmj.parking_place.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rmj.parking_place.R;

public class ParkingPlaceInfoFragment extends Fragment {

    public static ParkingPlaceInfoFragment newInstance() {

        ParkingPlaceInfoFragment pif = new ParkingPlaceInfoFragment();

        return pif;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView  = inflater.inflate(R.layout.parking_place_info_layout, container, false);

        return rootView;
    }
}
