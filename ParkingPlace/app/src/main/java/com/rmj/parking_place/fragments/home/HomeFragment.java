package com.rmj.parking_place.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.MainActivity;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public HomeFragment() {

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) getActivity();
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        Button mapPageBtn = root.findViewById(R.id.mapPageBtn);
        mapPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.changeFragmentInMainActivity(mainActivity, v.getId());
            }
        });

        Button reservationAndPaidParkingPlacesBtn = root.findViewById(R.id.reservationAndPaidParkingPlacesBtn);
        reservationAndPaidParkingPlacesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.changeFragmentInMainActivity(mainActivity, v.getId());
            }
        });

        Button favoritePlacesBtn = root.findViewById(R.id.favoritePlacesBtn);
        favoritePlacesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.changeFragmentInMainActivity(mainActivity, v.getId());
            }
        });

        Button registerUserBtn = root.findViewById(R.id.registerUserBtn);
        registerUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.changeFragmentInMainActivity(mainActivity, v.getId());
            }
        });

        return root;
    }
}
