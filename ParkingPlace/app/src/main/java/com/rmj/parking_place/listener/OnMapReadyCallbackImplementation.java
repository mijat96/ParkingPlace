package com.rmj.parking_place.listener;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.rmj.parking_place.R;
import com.rmj.parking_place.fragments.MapFragment;
import com.rmj.parking_place.fragments.MapPageFragment;

import java.util.List;

public class  OnMapReadyCallbackImplementation implements OnMapReadyCallback {
    private MapFragment mapFragment;
    private MapPageFragment mapPageFragment;

    public OnMapReadyCallbackImplementation(MapFragment mapFragment, MapPageFragment mapPageFragment) {
        this.mapFragment = mapFragment;
        this.mapPageFragment = mapPageFragment;
    }

    /**
     * KAda je mapa spremna mozemo da radimo sa njom.
     * Mozemo reagovati na razne dogadjaje dodavanje markera, pomeranje markera,klik na mapu,...
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapFragment.setMap(googleMap);
        /*if (mapFragment.isRecoveredFragment()) {
            mapFragment.restoreParkingPlaceMarkers();
            mapFragment.restoreNavigationPathPolyline();
        }*/
        mapFragment.setCurrentLocation(null);

        ClusteringSettings clusteringSettings = new ClusteringSettings();
        clusteringSettings.addMarkersDynamically(true);
        clusteringSettings.clusterSize(96);
        googleMap.setClustering(clusteringSettings.clusterOptionsProvider(new ClusterOptionsProvider() {
            @Override
            public ClusterOptions getClusterOptions(List<Marker> markers) {
                float hue;
                int numberOfEmpty = 0;

                for (Marker m: markers) {
                    if(m.getData() == null){
                        continue;
                    }
                    if(m.getData().toString().equals("EMPTY")){
                        numberOfEmpty++;
                    }
                }

                Bitmap bitmap = BitmapFactory.decodeResource(mapFragment.getContext().getResources(),
                        R.drawable.cluster_48);
                Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Rect bounds = new Rect();
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

                paint.setColor(Color.RED);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(mapFragment.getResources().getDimension(R.dimen.text_size_cluster));

                String textEmptyParking = String.valueOf(numberOfEmpty);
                String textAllParking = String.valueOf(markers.size());
                String text = textAllParking + "|" + textEmptyParking;
                paint.getTextBounds(text, 0, text.length(), bounds);
                float x = mutableBitmap.getWidth() / 2.0f;
                float y = (mutableBitmap.getHeight() - bounds.height()) / 2.0f - bounds.top;

                Canvas canvas = new Canvas(mutableBitmap);
                canvas.drawText(text, x, y, paint);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(mutableBitmap);

                return new ClusterOptions().icon(icon);
            }
        }));

        if (mapFragment.checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(mapFragment.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mapFragment.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                if (mapFragment.getProvider() == null) {
                    mapFragment.setProvider();
                }
                //Request location updates:
                Location currentLocation = mapFragment.getLocationManager().getLastKnownLocation(mapFragment.getProvider());
                mapFragment.setCurrentLocation(currentLocation);

                googleMap.setMyLocationEnabled(true);
                googleMap.setBuildingsEnabled(true);
                //map.getUiSettings().
            }
        }

        googleMap.setOnMapClickListener(new OnMapClickListenerImplementation(mapFragment, mapPageFragment));
        googleMap.setOnMarkerClickListener(new OnMarkerClickListenerImplementation(mapFragment, mapPageFragment));
        googleMap.setOnCameraChangeListener(new OnCameraChangeListenerImplementation(googleMap, mapPageFragment));
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoContents(Marker marker) {
                Context context = mapFragment.getActivity(); // getApplicationContext(); //or getActivity(), YourActivity.this, etc.

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }
        });

        Location currentLocation = mapFragment.getCurrentLocation();
        if (currentLocation != null) {
            mapFragment.updateCameraPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                                                                                    true);
        }

        mapFragment.drawParkingPlaceMarkersIfCan();

        if (mapPageFragment.isInIsReservingMode() || mapPageFragment.isInCanReserveAndCanTakeMode()) {
            mapFragment.redrawNavigationPath();
        }

        mapFragment.drawFavoritePlaceMarkers();

        mapFragment.changePositionOfMyLocationButton(true);
    }

}
