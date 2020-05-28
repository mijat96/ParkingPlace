package com.rmj.parking_place.dto;

import com.rmj.parking_place.model.TicketType;

public class TakingDTO extends ReservingDTO {
    private TicketType ticketType;


    public TakingDTO() {

    }

    public TakingDTO(double currentLocationLatitude, double currentLocationLongitude) {
        super(currentLocationLatitude, currentLocationLongitude);
        // this.ticketType = ticketType;
    }

    public TakingDTO(Long zoneId, Long parkingPlaceId, double currentLocationLatitude, double currentLocationLongitude) {
        super(zoneId, parkingPlaceId, currentLocationLatitude, currentLocationLongitude);
        // this.ticketType = ticketType;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }
}
