package com.rmj.parking_place.service;

import com.rmj.parking_place.dto.navigation.NavigationDTO;
import com.rmj.parking_place.model.FavoritePlace;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface NavigationService {


    @Headers({
            "User-Agent: Mobile-Android"
    })
    @GET("/api/1.0/gosmore.php")
    Call<NavigationDTO> getNavigation(@Query("flat") double flat, @Query("flon") double flon,
                                     @Query("tlat") double tlat, @Query("tlon") double tlon,
                                     @Query("format") String format);
}
