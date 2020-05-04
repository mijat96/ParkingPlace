package com.rmj.parking_place.utils;

import com.rmj.parking_place.model.FromTo;

public class NavigationResponse extends Response {
    private FromTo fromTo;

    public NavigationResponse() {

    }

    public NavigationResponse(HttpRequestAndResponseType type, String result, FromTo fromTo) {
        super(type, result);
        this.fromTo = fromTo;
    }

    public FromTo getFromTo() {
        return fromTo;
    }

    public void setFromTo(FromTo fromTo) {
        this.fromTo = fromTo;
    }
}
