package com.rmj.parking_place.dto.navigation;

public class CrsDTO {
    private String type;
    private CrsPropertiesDTO properties;

    public CrsDTO() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CrsPropertiesDTO getProperties() {
        return properties;
    }

    public void setProperties(CrsPropertiesDTO properties) {
        this.properties = properties;
    }
}
