package com.rmj.parking_place.dto;

import android.graphics.Bitmap;

public class ReportDTO extends DTO{
    Bitmap image;

    public ReportDTO(Long zoneId, Long parkingPlaceId, Bitmap image){
        super(zoneId, parkingPlaceId);
        this.image = image;
    }
}
