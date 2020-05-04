package com.rmj.parking_place.utils;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

public class PostRequestTask extends RequestTask {
    private Object content;
    private static MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");

    public PostRequestTask(Object content) {
        super();
        this.content = content;
    }

    @Override
    protected Request prepareRequest() {
        String jsonContent = JsonLoader.convertToJson(this.content);

        Request.Builder builder = new Request.Builder()
                .header("Accept", "application/json")
                .header("Accept", "application/json")
                .post(RequestBody.create(jsonMediaType, jsonContent));


        if (jwtToken != null) {
            builder.header("token", jwtToken);
        }
        Request request = builder.url(url)
                .build();
        return request;
    }
}
