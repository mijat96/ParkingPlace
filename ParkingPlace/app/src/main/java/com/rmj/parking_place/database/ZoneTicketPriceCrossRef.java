package com.rmj.parking_place.database;

import androidx.room.Entity;

@Entity(primaryKeys = {"zoneId", "ticketPriceId"})
public class ZoneTicketPriceCrossRef {
    public long zoneId;
    public long ticketPriceId;

    public ZoneTicketPriceCrossRef() {

    }

    public ZoneTicketPriceCrossRef(long zoneId, long ticketPriceId) {
        this.zoneId = zoneId;
        this.ticketPriceId = ticketPriceId;
    }
}
