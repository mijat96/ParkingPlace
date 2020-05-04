package com.rmj.parking_place.utils;

public class Response {
    private HttpRequestAndResponseType type;
    private String result;

    public Response() {

    }

    public Response(HttpRequestAndResponseType type, String result) {
        this.type = type;
        this.result = result;
    }

    public HttpRequestAndResponseType getType() {
        return type;
    }

    public void setType(HttpRequestAndResponseType type) {
        this.type = type;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
