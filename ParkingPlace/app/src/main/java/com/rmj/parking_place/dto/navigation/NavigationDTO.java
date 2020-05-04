package com.rmj.parking_place.dto.navigation;

import java.util.List;

public class NavigationDTO {
    private String type;
    private CrsDTO crs;
    private List<double[]> coordinates;
    private PropertiesDTO properties;

    public NavigationDTO() {

    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CrsDTO getCrs() {
        return crs;
    }

    public void setCrs(CrsDTO crs) {
        this.crs = crs;
    }

    public List<double[]> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<double[]> coordinates) {
        this.coordinates = coordinates;
    }

    public PropertiesDTO getProperties() {
        return properties;
    }

    public void setProperties(PropertiesDTO properties) {
        this.properties = properties;
    }
}
