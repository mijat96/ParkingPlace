package com.rmj.parking_place.dto;

import java.util.List;

public class ReservationAndPaidParkingPlacesDTO {
    private ReservationDTO reservation;
    private PaidParkingPlaceDTO regularPaidParkingPlace;
    private List<PaidParkingPlaceDTO> paidParkingPlacesForFavoritePlaces;

    public ReservationAndPaidParkingPlacesDTO() {

    }

    public ReservationDTO getReservation() {
        return reservation;
    }

    public void setReservation(ReservationDTO reservation) {
        this.reservation = reservation;
    }

    public PaidParkingPlaceDTO getRegularPaidParkingPlace() {
        return regularPaidParkingPlace;
    }

    public void setRegularPaidParkingPlace(PaidParkingPlaceDTO regularPaidParkingPlace) {
        this.regularPaidParkingPlace = regularPaidParkingPlace;
    }

    public List<PaidParkingPlaceDTO> getPaidParkingPlacesForFavoritePlaces() {
        return paidParkingPlacesForFavoritePlaces;
    }

    public void setPaidParkingPlacesForFavoritePlaces(List<PaidParkingPlaceDTO> paidParkingPlacesForFavoritePlaces) {
        this.paidParkingPlacesForFavoritePlaces = paidParkingPlacesForFavoritePlaces;
    }
}
