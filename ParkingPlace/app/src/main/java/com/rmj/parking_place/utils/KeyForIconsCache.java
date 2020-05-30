package com.rmj.parking_place.utils;

import java.util.Objects;

public class KeyForIconsCache {
    private Integer totalNumber;
    private Integer numberOfEmpties;

    public KeyForIconsCache() {

    }

    public KeyForIconsCache(Integer totalNumber, Integer numberOfEmpties) {
        this.totalNumber = totalNumber;
        this.numberOfEmpties = numberOfEmpties;
    }


    public Integer getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(Integer totalNumber) {
        this.totalNumber = totalNumber;
    }

    public Integer getNumberOfEmpties() {
        return numberOfEmpties;
    }

    public void setNumberOfEmpties(Integer numberOfEmpties) {
        this.numberOfEmpties = numberOfEmpties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyForIconsCache that = (KeyForIconsCache) o;
        return totalNumber.equals(that.totalNumber) &&
                numberOfEmpties.equals(that.numberOfEmpties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalNumber, numberOfEmpties);
    }
}
