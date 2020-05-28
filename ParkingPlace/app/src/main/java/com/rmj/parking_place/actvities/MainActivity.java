package com.rmj.parking_place.actvities;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.rmj.parking_place.App;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.login.ui.LoginActivity;
import com.rmj.parking_place.fragments.FindParkingFragment;
import com.rmj.parking_place.fragments.MapPageFragment;
import com.rmj.parking_place.fragments.favorite_places.FavoritePlacesFragment;
import com.rmj.parking_place.model.FavoritePlace;
import com.rmj.parking_place.service.ParkingPlaceServerUtils;
import com.rmj.parking_place.utils.TokenUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends /*AppCompatActivity*/ CheckWifiActivity
                            implements FavoritePlacesFragment.OnListFragmentInteractionListener {

    private AppBarConfiguration mAppBarConfiguration;

    private ArrayList<FavoritePlace> favoritePlaces;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            loadFavoritePlaces();
        }
        else {
            favoritePlaces = savedInstanceState.getParcelableArrayList("favoritePlaces");
            if (favoritePlaces == null) {
                favoritePlaces = new ArrayList<FavoritePlace>();
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_map_page, R.id.nav_favorite_places)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (favoritePlaces != null) {
            outState.putParcelableArrayList("favoritePlaces", favoritePlaces);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void loadFavoritePlaces() {
       ParkingPlaceServerUtils.userService.getFavoritePlaces()
               .enqueue(new Callback<ArrayList<FavoritePlace>>() {
                   @Override
                   public void onResponse(Call<ArrayList<FavoritePlace>> call, Response<ArrayList<FavoritePlace>> response) {
                       if (response.isSuccessful()) {
                           favoritePlaces = response.body();
                           if (favoritePlaces == null) {
                               favoritePlaces = new ArrayList<FavoritePlace>();
                           }
                       }
                       else if(response.code() == 401) { // Unauthorized

                       }
                       else {
                           favoritePlaces = new ArrayList<FavoritePlace>();
                       }
                   }

                   @Override
                   public void onFailure(Call<ArrayList<FavoritePlace>> call, Throwable t) {
                       Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                       favoritePlaces = new ArrayList<FavoritePlace>();
                   }
               });
    }


    /*public void loginAgain() {
        String currentActivity = App.getCurrentActivityName();
        if (currentActivity.endsWith("LoginActivity")) { // vec se nalazi na login activity-ju
            return;
        }

        TokenUtils.removeToken();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
        finish();
    }*/

    public void clickOnItemLogout(MenuItem item) {
        TokenUtils.removeToken();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    @Override
    public void onListFragmentInteraction(FavoritePlace item) {
        Toast.makeText(this, item.getName(), Toast.LENGTH_SHORT).show();
    }

    public ArrayList<FavoritePlace> getFavoritePlaces() {
        return favoritePlaces;
    }
}
