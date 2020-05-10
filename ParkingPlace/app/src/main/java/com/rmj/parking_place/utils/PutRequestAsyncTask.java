package com.rmj.parking_place.utils;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

public class PutRequestAsyncTask extends RequestAsyncTask {
    private Object content;
    private static MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");

    public PutRequestAsyncTask(AsyncResponse delegate, Object content) {
        super(delegate);
        this.content = content;
    }

    @Override
    protected Request prepareRequest() {
        String jsonContent = JsonLoader.convertToJson(this.content);

        Request.Builder builder = new Request.Builder()
                .header("Accept", "application/json")
                .header("Accept", "application/json")
                .put(RequestBody.create(jsonMediaType, jsonContent));


        if (jwtToken != null) {
            builder.header("token", jwtToken);
        }
        Request request = builder.url(url)
                .build();
        return request;
    }
}
