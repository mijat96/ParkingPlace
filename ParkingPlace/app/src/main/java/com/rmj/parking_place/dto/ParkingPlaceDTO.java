package com.rmj.parking_place.dto;

import com.rmj.parking_place.model.ParkingPlaceStatus;

public class ParkingPlaceDTO {
    private Long id;
    private ParkingPlaceStatus status;


    public ParkingPlaceDTO() {

    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ParkingPlaceStatus getStatus() {
        return status;
    }

    public void setStatus(ParkingPlaceStatus status) {
        this.status = status;
    }
}
