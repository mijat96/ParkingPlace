package com.rmj.parking_place.dto;

import java.util.List;

public class ParkingPlacesUpdatingDTO {
    private List<ParkingPlaceChangesDTO> changes;
    private List<ParkingPlacesInitialDTO> initials;

    public ParkingPlacesUpdatingDTO() {

    }

    public List<ParkingPlaceChangesDTO> getChanges() {
        return changes;
    }

    public void setChanges(List<ParkingPlaceChangesDTO> changes) {
        this.changes = changes;
    }

    public List<ParkingPlacesInitialDTO> getInitials() {
        return initials;
    }

    public void setInitials(List<ParkingPlacesInitialDTO> initials) {
        this.initials = initials;
    }
}
