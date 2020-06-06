package com.rmj.parking_place.database;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Junction;
import androidx.room.Relation;

import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.TicketPrice;
import com.rmj.parking_place.model.Zone;

import java.util.ArrayList;
import java.util.List;

public class ZoneWithLocationsAndTicketPrices {
    @Embedded
    public ZoneDb zone;

    @Relation(
            parentColumn = "zoneId",
            entityColumn = "locationId",
            associateBy = @Junction(ZoneNorthEastLocationCrossRef.class)
    )
    public LocationDb northEast;

    @Relation(
            parentColumn = "zoneId",
            entityColumn = "locationId",
            associateBy = @Junction(ZoneSouthWestLocationCrossRef.class)
    )
    public LocationDb southWest;


    @Relation(
            parentColumn = "zoneId",
            entityColumn = "ticketPriceId",
            associateBy = @Junction(ZoneTicketPriceCrossRef.class)
    )
    public List<TicketPriceDb> ticketPrices;


    public ZoneWithLocationsAndTicketPrices() {

    }

}
