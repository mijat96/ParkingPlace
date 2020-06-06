package com.rmj.parking_place.fragments.favorite_places;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.AddOrEditFavoritePlaceActivity;
import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.model.FavoritePlace;
import com.rmj.parking_place.model.FavoritePlaceType;
import com.rmj.parking_place.model.Location;


import static android.app.Activity.RESULT_OK;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FavoritePlacesFragment extends Fragment {

    private FavoritePlacesListAdapter adapter;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    protected static final int ADD_OR_EDIT_FAVORITE_PLACE_REQUEST = 998;
    // private ArrayList<FavoritePlace> favoritePlaces;
    private MainActivity mainActivity;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavoritePlacesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static FavoritePlacesFragment newInstance(int columnCount) {
        FavoritePlacesFragment fragment = new FavoritePlacesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        /*if (savedInstanceState != null) {
            // recovering the instance state
            favoritePlaces = savedInstanceState.getParcelableArrayList("favoritePlaces");
        }
        else {
            favoritePlaces = new ArrayList<FavoritePlace>();
            Call<ArrayList<FavoritePlace>> call = ParkingPlaceServiceUtils.userService.getFavoritePlaces();
            call.enqueue(new Callback<ArrayList<FavoritePlace>>() {
                @Override
                public void onResponse(Call<ArrayList<FavoritePlace>> call, Response<ArrayList<FavoritePlace>> response) {
                    if (response.isSuccessful()) {
                        favoritePlaces = response.body();
                        adapter.setItems(favoritePlaces);
                    }
                    else if(response.code() == 401) { // Unauthorized
                        loginAgain();
                    }
                    else {
                        favoritePlaces = new ArrayList<FavoritePlace>();
                        adapter.setItems(favoritePlaces);
                        Toast.makeText(getActivity(), "Problem with loading favorite places", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<FavoritePlace>> call, Throwable t) {
                    favoritePlaces = new ArrayList<FavoritePlace>();
                    adapter.setItems(favoritePlaces);
                    Toast.makeText(getActivity(), "Problem with loading favorite places", Toast.LENGTH_SHORT).show();
                }
            });

        }*/
    }

    /*@Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("favoritePlaces", favoritePlaces);

        super.onSaveInstanceState(outState);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_places, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewId);

        // Set the adapter
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        adapter = new FavoritePlacesListAdapter(mainActivity.getFavoritePlaces(), mListener, this);
        recyclerView.setAdapter(adapter);

        ImageButton addButton = view.findViewById(R.id.addBtn);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnAddButton(v);
            }
        });

        return view;
    }

    /*public void loginAgain() {
        mainActivity.loginAgain();
    }*/

    private void clickOnAddButton(View v) {
        if (adapter.canAdd()) {
            goToAddOrEditFavoritePlaceActivity();
        }
        else {
            Toast.makeText(getActivity(), "MAX_FAVORITE_PLACES == 3", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToAddOrEditFavoritePlaceActivity() {
        Intent intent = new Intent(getActivity(), AddOrEditFavoritePlaceActivity.class);
        startActivityForResult(intent, ADD_OR_EDIT_FAVORITE_PLACE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_OR_EDIT_FAVORITE_PLACE_REQUEST && resultCode == RESULT_OK) {
            Location selectedLocation = (Location) data.getParcelableExtra("selected_location");
            String favoritePlaceName = data.getStringExtra("favorite_place_name");
            String favoritePlaceTypeStr = data.getStringExtra("favoritePlaceType");
            FavoritePlaceType favoritePlaceType = FavoritePlaceType.valueOf(favoritePlaceTypeStr);
            adapter.addOrUpdateItem(favoritePlaceName, favoritePlaceType, selectedLocation);
        }
        else {
            adapter.setItemForEditing(null);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(FavoritePlace item);
    }
}
