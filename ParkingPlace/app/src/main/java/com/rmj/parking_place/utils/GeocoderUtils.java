package com.rmj.parking_place.utils;

import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.rmj.parking_place.App;

import java.io.IOException;
import java.util.List;

public class GeocoderUtils {

    private static Geocoder geocoder = new Geocoder(App.getAppContext());

    public static String getAddressFromLatLng(LatLng latLng) {
        String addressStr;
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1);
            if (addresses.isEmpty()) {
                addressStr = "Unknown";
            }
            else {
                Address a = addresses.get(0);
                addressStr = a.getAddressLine(0);
                // String city = a.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
            addressStr = "Error";
        }

        return addressStr;
    }

    public static LatLng getLatLngFromLocationName(String locationName) {
        LatLng latLng;
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1,
                                                    45.222314, 19.767787,
                                                    45.308942,19.900966);
            // trazi adresu unutar granica Novog Sada
            if (addresses.isEmpty()) {
                latLng = null;
            }
            else {
                Address a = addresses.get(0);
                latLng = new LatLng(a.getLatitude(), a.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
            latLng = null;
        }

        return latLng;
    }
}
