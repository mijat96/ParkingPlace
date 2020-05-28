package com.rmj.parking_place.service;

import com.rmj.parking_place.dto.ParkingPlacesUpdatingDTO;
import com.rmj.parking_place.model.FavoritePlace;
import com.rmj.parking_place.model.Zone;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface ZoneService {

    @Headers({
            "User-Agent: Mobile-Android"
    })
    @GET("/api/zones")
    Call<List<Zone>> getZones();

    @Headers({
            "User-Agent: Mobile-Android"
    })
    @GET("/api/parkingplaces/changes")
    Call<ParkingPlacesUpdatingDTO> updateZonesWithParkingPlaces(@Query("zoneids") String zoneids,
                                                                @Query("versions") String versions);

}
