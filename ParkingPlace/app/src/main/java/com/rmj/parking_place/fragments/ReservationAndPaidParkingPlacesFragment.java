package com.rmj.parking_place.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.model.PaidParkingPlace;
import com.rmj.parking_place.model.Reservation;
import com.rmj.parking_place.model.Zone;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReservationAndPaidParkingPlacesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReservationAndPaidParkingPlacesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;
    private MainActivity mainActivity;

    private Reservation reservation;
    private PaidParkingPlace regularPaidParkingPlace;
    private List<PaidParkingPlace> paidParkingPlacesForFavoritePlaces;

    public ReservationAndPaidParkingPlacesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReservationAndPaidParkingPlacesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReservationAndPaidParkingPlacesFragment newInstance(String param1, String param2) {
        ReservationAndPaidParkingPlacesFragment fragment = new ReservationAndPaidParkingPlacesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mainActivity = (MainActivity) getActivity();

        if (savedInstanceState == null) {
            reservation = mainActivity.getReservation();
            regularPaidParkingPlace = mainActivity.getRegularPaidParkingPlace();
            paidParkingPlacesForFavoritePlaces = mainActivity.getPaidParkingPlacesForFavoritePlaces();
        }
        else {
            reservation = savedInstanceState.getParcelable("reservation");
            regularPaidParkingPlace = savedInstanceState.getParcelable("regularPaidParkingPlace");
            paidParkingPlacesForFavoritePlaces = savedInstanceState.getParcelableArrayList("paidParkingPlacesForFavoritePlaces");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reservation_and_paid_parking_places, container, false);

        showDataForReservation();
        showDataForRegularPaidParkingPlace();
        showDataForPaidParkingPlaceForFavoritePlace();

        return view;
    }

    private void showDataForReservation() {
        if (reservation == null) {
            view.findViewById(R.id.reservationData).setVisibility(View.GONE);
            view.findViewById(R.id.noReservation).setVisibility(View.VISIBLE);
        }
        else {
            TextView startAndEndDateTimeAndroidReservation = view.findViewById(R.id.startAndEndDateTimeAndroidReservation);
            startAndEndDateTimeAndroidReservation.setText("Android Date Time:  " +reservation.getStartAndEndDateTimeAndroid());

            TextView startAndEndDateTimeServerReservation = view.findViewById(R.id.startAndEndDateTimeServerReservation);
            startAndEndDateTimeServerReservation.setText("Server Date Time:  " + reservation.getStartAndEndDateTimeServer());

            Location location = reservation.getParkingPlace().getLocation();

            TextView latitudeReservation = view.findViewById(R.id.latitudeReservation);
            latitudeReservation.setText(""+location.getLatitude());

            TextView longitudeReservation = view.findViewById(R.id.longitudeReservation);
            longitudeReservation.setText(""+location.getLongitude());

            TextView addressReservation = view.findViewById(R.id.addressReservation);
            addressReservation.setText(""+location.getAddress());

            view.findViewById(R.id.reservationData).setVisibility(View.VISIBLE);
            view.findViewById(R.id.noReservation).setVisibility(View.GONE);
        }
    }

    private void showDataForRegularPaidParkingPlace() {
        if (regularPaidParkingPlace == null) {
            view.findViewById(R.id.regularPaidParkingPlaceData).setVisibility(View.GONE);
            view.findViewById(R.id.noRegularPaidParkingPlace).setVisibility(View.VISIBLE);
        }
        else {
            TextView startAndEndDateTimeAndroidRegularPaidParkingPlace = view.findViewById(R.id.startAndEndDateTimeAndroidRegularPaidParkingPlace);
            startAndEndDateTimeAndroidRegularPaidParkingPlace.setText("Android Date Time:  "
                                                                + regularPaidParkingPlace.getStartAndEndDateTimeAndroid());

            TextView startAndEndDateTimeServerRegularPaidParkingPlace = view.findViewById(R.id.startAndEndDateTimeServerRegularPaidParkingPlace);
            startAndEndDateTimeServerRegularPaidParkingPlace.setText("Server Date Time:  "
                                                                + regularPaidParkingPlace.getStartAndEndDateTimeServer());

            Location location = regularPaidParkingPlace.getParkingPlace().getLocation();

            TextView latitudeRegularPaidParkingPlace = view.findViewById(R.id.latitudeRegularPaidParkingPlace);
            latitudeRegularPaidParkingPlace.setText(""+location.getLatitude());

            TextView longitudeRegularPaidParkingPlace = view.findViewById(R.id.longitudeRegularPaidParkingPlace);
            longitudeRegularPaidParkingPlace.setText(""+location.getLongitude());

            TextView addressRegularPaidParkingPlace = view.findViewById(R.id.addressRegularPaidParkingPlace);
            addressRegularPaidParkingPlace.setText(""+location.getAddress());

            view.findViewById(R.id.regularPaidParkingPlaceData).setVisibility(View.VISIBLE);
            view.findViewById(R.id.noRegularPaidParkingPlace).setVisibility(View.GONE);
        }
    }

    private void showDataForPaidParkingPlaceForFavoritePlace() {
        if (paidParkingPlacesForFavoritePlaces == null || (paidParkingPlacesForFavoritePlaces != null
                                                            && paidParkingPlacesForFavoritePlaces.isEmpty())) {
            view.findViewById(R.id.paidParkingPlacesNearbyFavoritePlaces).setVisibility(View.GONE);
            view.findViewById(R.id.noPaidParkingPlacesNearbyFavoritePlaces).setVisibility(View.VISIBLE);
        }
        else {
            PaidParkingPlace firstPaidParkingPlace = paidParkingPlacesForFavoritePlaces.get(0);
            TextView startAndEndDateTimeFirstPaidParkingPlace = view.findViewById(R.id.startAndEndDateTimeAndroidFirstPaidParkingPlace);
            startAndEndDateTimeFirstPaidParkingPlace.setText("Android Date Time:  "
                                                            + firstPaidParkingPlace.getStartAndEndDateTimeAndroid());
            TextView startAndEndDateTimeServerFirstPaidParkingPlace = view.findViewById(R.id.startAndEndDateTimeServerFirstPaidParkingPlace);
            startAndEndDateTimeServerFirstPaidParkingPlace.setText("Server Date Time:  "
                                                            + firstPaidParkingPlace.getStartAndEndDateTimeServer());
            Location firstPaidParkingPlaceLocation = firstPaidParkingPlace.getParkingPlace().getLocation();
            TextView latitudeFirstPaidParkingPlace = view.findViewById(R.id.latitudeFirstPaidParkingPlace);
            latitudeFirstPaidParkingPlace.setText(""+firstPaidParkingPlaceLocation.getLatitude());
            TextView longitudeFirstPaidParkingPlace = view.findViewById(R.id.longitudeFirstPaidParkingPlace);
            longitudeFirstPaidParkingPlace.setText(""+firstPaidParkingPlaceLocation.getLongitude());
            TextView addressFirstPaidParkingPlace = view.findViewById(R.id.addressFirstPaidParkingPlace);
            addressFirstPaidParkingPlace.setText(""+firstPaidParkingPlaceLocation.getAddress());

            if (paidParkingPlacesForFavoritePlaces.size() > 1) {
                PaidParkingPlace secondPaidParkingPlace = paidParkingPlacesForFavoritePlaces.get(1);
                TextView startAndEndDateTimeAndroidSecondPaidParkingPlace = view.findViewById(R.id.startAndEndDateTimeAndroidSecondPaidParkingPlace);
                startAndEndDateTimeAndroidSecondPaidParkingPlace.setText("Android Date Time:  "
                        + secondPaidParkingPlace.getStartAndEndDateTimeAndroid());
                TextView startAndEndDateTimeServerSecondPaidParkingPlace = view.findViewById(R.id.startAndEndDateTimeServerSecondPaidParkingPlace);
                startAndEndDateTimeServerSecondPaidParkingPlace.setText("Server Date Time:  "
                        + secondPaidParkingPlace.getStartAndEndDateTimeServer());
                Location secondPaidParkingPlaceLocation = secondPaidParkingPlace.getParkingPlace().getLocation();
                TextView latitudeSecondPaidParkingPlace = view.findViewById(R.id.latitudeSecondPaidParkingPlace);
                latitudeSecondPaidParkingPlace.setText(""+secondPaidParkingPlaceLocation.getLatitude());
                TextView longitudeSecondPaidParkingPlace = view.findViewById(R.id.longitudeSecondPaidParkingPlace);
                longitudeSecondPaidParkingPlace.setText(""+secondPaidParkingPlaceLocation.getLongitude());
                TextView addressSecondPaidParkingPlace = view.findViewById(R.id.addressSecondPaidParkingPlace);
                addressSecondPaidParkingPlace.setText(""+secondPaidParkingPlaceLocation.getAddress());
            }
            else {
                view.findViewById(R.id.secondPaidParkingPlaceData).setVisibility(View.GONE);
            }

            if (paidParkingPlacesForFavoritePlaces.size() > 2) {
                PaidParkingPlace thirdPaidParkingPlace = paidParkingPlacesForFavoritePlaces.get(2);
                TextView startAndEndDateTimeAndroidThirdPaidParkingPlace = view.findViewById(R.id.startAndEndDateTimeAndroidThirdPaidParkingPlace);
                startAndEndDateTimeAndroidThirdPaidParkingPlace.setText("Android Date Time:  "
                                                            + thirdPaidParkingPlace.getStartAndEndDateTimeAndroid());
                TextView startAndEndDateTimeServerThirdPaidParkingPlace = view.findViewById(R.id.startAndEndDateTimeServerThirdPaidParkingPlace);
                startAndEndDateTimeServerThirdPaidParkingPlace.setText("Server Date Time:  "
                                                            + thirdPaidParkingPlace.getStartAndEndDateTimeServer());
                Location thirdPaidParkingPlaceLocation = thirdPaidParkingPlace.getParkingPlace().getLocation();
                TextView latitudeThirdPaidParkingPlace = view.findViewById(R.id.latitudeThirdPaidParkingPlace);
                latitudeThirdPaidParkingPlace.setText(""+thirdPaidParkingPlaceLocation.getLatitude());
                TextView longitudeThirdPaidParkingPlace = view.findViewById(R.id.longitudeThirdPaidParkingPlace);
                longitudeThirdPaidParkingPlace.setText(""+thirdPaidParkingPlaceLocation.getLongitude());
                TextView addressThirdPaidParkingPlace = view.findViewById(R.id.addressThirdPaidParkingPlace);
                addressThirdPaidParkingPlace.setText(""+thirdPaidParkingPlaceLocation.getAddress());
            }
            else {
                view.findViewById(R.id.thirdPaidParkingPlaceData).setVisibility(View.GONE);
            }

            view.findViewById(R.id.paidParkingPlacesNearbyFavoritePlaces).setVisibility(View.VISIBLE);
            view.findViewById(R.id.noPaidParkingPlacesNearbyFavoritePlaces).setVisibility(View.GONE);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (reservation != null) {
            outState.putParcelable("reservation", reservation);
        }

        if (regularPaidParkingPlace != null) {
            outState.putParcelable("regularPaidParkingPlace", regularPaidParkingPlace);
        }

        if (paidParkingPlacesForFavoritePlaces != null) {
            outState.putParcelableArrayList("paidParkingPlacesForFavoritePlaces",
                    (ArrayList<PaidParkingPlace>) paidParkingPlacesForFavoritePlaces);
        }

        super.onSaveInstanceState(outState);
    }

}