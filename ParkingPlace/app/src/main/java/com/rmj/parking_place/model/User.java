package com.rmj.parking_place.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private String registrationNumber;
    private int currentNumberOfReservationViolations;
    private int totalNumberOfReservationViolations;
    private List<Location> favoritePlaces;
    private Reservation reservation;
    private PaidParkingPlace regularPaidParkingPlace;
    private List<PaidParkingPlace> paidParkingPlacesForFavoritePlaces;
    private List<Punishment> punishments;
    private Punishment activePunishment;


    public User() {

    }

    public User(String username, String password, String registrationNumber) {
        this.username = username;
        this.password = password;
        this.registrationNumber = registrationNumber;
        this.currentNumberOfReservationViolations = 0;
        this.totalNumberOfReservationViolations = 0;
        this.favoritePlaces = new ArrayList<Location>();
        this.reservation = null;
        this.regularPaidParkingPlace = null;
        this.paidParkingPlacesForFavoritePlaces = new ArrayList<PaidParkingPlace>();
        this.punishments = new ArrayList<Punishment>();
        this.activePunishment = null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public int getCurrentNumberOfReservationViolations() {
        return currentNumberOfReservationViolations;
    }

    public void setCurrentNumberOfReservationViolations(int currentNumberOfReservationViolations) {
        this.currentNumberOfReservationViolations = currentNumberOfReservationViolations;
    }

    public int getTotalNumberOfReservationViolations() {
        return totalNumberOfReservationViolations;
    }

    public void setTotalNumberOfReservationViolations(int totalNumberOfReservationViolations) {
        this.totalNumberOfReservationViolations = totalNumberOfReservationViolations;
    }

    public List<Location> getFavoritePlaces() {
        return favoritePlaces;
    }

    public void setFavoritePlaces(List<Location> favoritePlaces) {
        this.favoritePlaces = favoritePlaces;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public PaidParkingPlace getRegularPaidParkingPlace() {
        return regularPaidParkingPlace;
    }

    public void setRegularPaidParkingPlace(PaidParkingPlace regularPaidParkingPlace) {
        this.regularPaidParkingPlace = regularPaidParkingPlace;
    }

    public List<PaidParkingPlace> getPaidParkingPlacesForFavoritePlaces() {
        return paidParkingPlacesForFavoritePlaces;
    }

    public void setPaidParkingPlacesForFavoritePlaces(List<PaidParkingPlace> paidParkingPlacesForFavoritePlaces) {
        this.paidParkingPlacesForFavoritePlaces = paidParkingPlacesForFavoritePlaces;
    }

    public List<Punishment> getPunishments() {
        return punishments;
    }

    public void setPunishments(List<Punishment> punishments) {
        this.punishments = punishments;
    }

    public Punishment getActivePunishment() {
        return activePunishment;
    }

    public void setActivePunishment(Punishment activePunishment) {
        this.activePunishment = activePunishment;
    }

}
