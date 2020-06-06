package com.rmj.parking_place.database;

import androidx.annotation.InspectableProperty;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.rmj.parking_place.model.TicketPrice;

@Entity(tableName = "ticket_price")
public class TicketPriceDb {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public long ticketPriceId;
    public int duration; // hours
    public int ticketType;
    public float price;

    public TicketPriceDb() {

    }

    public TicketPriceDb(TicketPrice ticketPrice) {
        this.duration = ticketPrice.getDuration();
        this.ticketType = ticketPrice.getTicketType().ordinal();
        this.price = ticketPrice.getPrice();
    }
}
