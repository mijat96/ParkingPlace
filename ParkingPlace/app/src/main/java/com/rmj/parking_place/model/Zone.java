package com.rmj.parking_place.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Zone {
    private Long id;
    private String name;
    private Long version;
    private Location northEast;
    private Location southWest;
    private List<ParkingPlace> parkingPlaces;
    private List<TicketPrice> ticketPrices;

    public Zone() {

    }

    public Zone(Long id, String name, Long version, Location northEast, Location southWest,
                    List<ParkingPlace> parkingPlaces, List<TicketPrice> ticketPrices) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.northEast = northEast;
        this.southWest = southWest;
        this.parkingPlaces = parkingPlaces;
        this.ticketPrices = ticketPrices;
    }

    public Zone(Zone zone) {
        this.id = zone.id;
        this.name = zone.name;
        this.parkingPlaces = null;
        this.version = null;
        this.northEast = null;
        this.southWest = null;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getNorthEast() {
        return northEast;
    }

    public void setNorthEast(Location northEast) {
        this.northEast = northEast;
    }

    public Location getSouthWest() {
        return southWest;
    }

    public void setSouthWest(Location southWest) {
        this.southWest = southWest;
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
