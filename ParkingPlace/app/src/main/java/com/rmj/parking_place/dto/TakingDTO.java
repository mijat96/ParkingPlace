package com.rmj.parking_place.dto;

import com.rmj.parking_place.model.TicketType;

public class TakingDTO extends ReservationDTO {
    private TicketType ticketType;


    public TakingDTO() {

    }

    public TakingDTO(Long zoneId, Long parkingPlaceId) {
        super(zoneId, parkingPlaceId);
        this.ticketType = TicketType.REGULAR;
    }

    public TakingDTO(Long zoneId, Long parkingPlaceId, TicketType ticketType) {
        super(zoneId, parkingPlaceId);
        this.ticketType = ticketType;
    }


    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }
}
