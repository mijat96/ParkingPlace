package com.rmj.parking_place.model;

import java.util.List;

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
}
