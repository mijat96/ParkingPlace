package com.rmj.parking_place.utils;


import com.squareup.okhttp.Request;

public class GetRequestAsyncTask extends RequestAsyncTask {

    public GetRequestAsyncTask(AsyncResponse delegate) {
        super(delegate);
    }

    @Override
    protected Request prepareRequest() {
        Request.Builder builder = new Request.Builder()
                                        .header("Accept", "application/json");
        if (jwtToken != null) {
            builder.header("token", jwtToken);
        }
        Request request = builder.url(url)
                                .build();
        return request;
    }
}
