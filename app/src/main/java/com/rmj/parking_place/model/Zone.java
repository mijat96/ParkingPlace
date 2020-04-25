package com.rmj.parking_place.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Zone {
    private String name;
    private List<ParkingPlace> parkingPlaces;
    private List<TicketPrice> ticketPrices;

    public Zone() {

    }

    public Zone(String name, List<ParkingPlace> parkingPlaces, List<TicketPrice> ticketPrices) {
        this.name = name;
        this.parkingPlaces = parkingPlaces;
        this.ticketPrices = ticketPrices;
    }

    public Zone(Zone zone) {
        this.name = zone.name;
        this.parkingPlaces = null;
        this.ticketPrices = new ArrayList<TicketPrice>();
        for (TicketPrice ticketPrice : zone.ticketPrices) {
            this.ticketPrices.add(ticketPrice);
        }
    }

    public TicketPrice getTicketPrice(TicketType ticketType) {
        for (TicketPrice ticketPrice : ticketPrices) {
            if (ticketPrice.getTicketType() == ticketType) {
                return ticketPrice;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParkingPlace> getParkingPlaces() {
        return parkingPlaces;
    }

    public void setParkingPlaces(List<ParkingPlace> parkingPlaces) {
        this.parkingPlaces = parkingPlaces;
    }

    public List<TicketPrice> getTicketPrices() {
        return ticketPrices;
    }

    public void setTicketPrices(List<TicketPrice> ticketPrices) {
        this.ticketPrices = ticketPrices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zone zone = (Zone) o;
        return name.equals(zone.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
