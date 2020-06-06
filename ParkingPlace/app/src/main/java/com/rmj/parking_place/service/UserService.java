package com.rmj.parking_place.service;

import com.rmj.parking_place.dto.RegistrationDTO;
import com.rmj.parking_place.dto.ReservationAndPaidParkingPlacesDTO;
import com.rmj.parking_place.dto.TokenDTO;
import com.rmj.parking_place.model.FavoritePlace;

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


public interface UserService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @POST("/api/users/favorite-places/add-or-update")
    Call<Long> addOrUpdateFavoritePlace(@Body FavoritePlace favoritePlace);

    @Headers({
            "User-Agent: Mobile-Android"
    })
    @DELETE("/api/users/favorite-places/{favoritePlaceId}/remove")
    Call<ResponseBody> removeFavoritePlace(@Path("favoritePlaceId") Long favoritePlaceId);

    @Headers({
            "User-Agent: Mobile-Android"
    })
    @GET("/api/users/favorite-places")
    Call<ArrayList<FavoritePlace>> getFavoritePlaces();

    @Headers({
            "User-Agent: Mobile-Android"
    })
    @GET("/api/users/reservation-and-paid-parking-places")
    Call<ReservationAndPaidParkingPlacesDTO> getReservationAndPaidParkingPlaces();
}
