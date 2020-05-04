package com.rmj.parking_place.dto.navigation;

public class PropertiesDTO {
    private double distance;
    private String description;
    private int traveltime; // sec

    public PropertiesDTO() {

    }


    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTraveltime() {
        return traveltime;
    }

    public void setTraveltime(int traveltime) {
        this.traveltime = traveltime;
    }
}
