package com.rmj.parking_place.dto;

public class RegistrationDTO {
    private String username;
    private String password;
    private String repeatPassword;
    private String carRegistrationNumber;

    public RegistrationDTO() {

    }

    public RegistrationDTO(String username, String password, String repeatPassword, String carRegistrationNumber) {
        this.username = username;
        this.password = password;
        this.repeatPassword = repeatPassword;
        this.carRegistrationNumber = carRegistrationNumber;
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

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    public String getCarRegistrationNumber() {
        return carRegistrationNumber;
    }

    public void setCarRegistrationNumber(String carRegistrationNumber) {
        this.carRegistrationNumber = carRegistrationNumber;
    }
}
