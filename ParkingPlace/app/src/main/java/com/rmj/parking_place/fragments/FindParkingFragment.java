package com.rmj.parking_place.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rmj.parking_place.R;

public class FindParkingFragment extends Fragment {

    public static FindParkingFragment newInstance() {

        FindParkingFragment fpf = new FindParkingFragment();

        return fpf;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView  = inflater.inflate(R.layout.find_parking_layout, container, false);

        return rootView;
    }
}
