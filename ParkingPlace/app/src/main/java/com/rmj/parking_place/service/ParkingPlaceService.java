package com.rmj.parking_place.service;

import com.rmj.parking_place.dto.DTO;
import com.rmj.parking_place.dto.LoginDTO;
import com.rmj.parking_place.dto.PaidParkingPlaceDTO;
import com.rmj.parking_place.dto.RegistrationDTO;
import com.rmj.parking_place.dto.ReservationDTO;
import com.rmj.parking_place.dto.ReservingDTO;
import com.rmj.parking_place.dto.TakingDTO;
import com.rmj.parking_place.dto.TokenDTO;
import com.rmj.parking_place.model.ParkingPlace;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;


public interface ParkingPlaceService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @POST("/api/parkingplaces/reservation")
    Call<ReservationDTO> reserveParkingPlace(@Body ReservingDTO reservingDTO);


    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @POST("/api/parkingplaces/taking")
    Call<PaidParkingPlaceDTO> takeParkingPlace(@Body TakingDTO takingDTO);


    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @PUT("/api/parkingplaces/leave")
    Call<ResponseBody> leaveParkingPlace(@Body DTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @GET("/api/parking/place/getParking/{id}")
    Call<ParkingPlace> getParkingPlace(@Path("id") Long id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @GET("/api/parking/place/againTake/{id}")
    Call<ParkingPlace> againTakeParkinPlace(@Path("id") Long id);



}
