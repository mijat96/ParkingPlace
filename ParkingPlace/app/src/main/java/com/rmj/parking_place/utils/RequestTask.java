package com.rmj.parking_place.utils;

import android.os.AsyncTask;

import com.rmj.parking_place.exceptions.InvalidNumberOfParamsException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public abstract class RequestTask {
    private static long CONNECTION_TIMEOUT = 60; // sec
    private static long READ_TIMEOUT = 60; // sec
    private static long WRITE_TIMEOUT = 60; // sec

    private static OkHttpClient client;
    static {
        client = new OkHttpClient();
        client.setConnectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
        client.setWriteTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
    }

    protected HttpRequestAndResponseType httpRequestAndResponseType;

    protected String url;
    protected String jwtToken;


    public RequestTask() {

    }

    protected abstract Request prepareRequest();

    public Object execute(String... params) {
        if (params.length < 2) {
            throw new InvalidNumberOfParamsException("params.length < 2");
        }

        url = params[0];
        httpRequestAndResponseType = HttpRequestAndResponseType.valueOf(params[1]);
        if (params.length == 3) {
            jwtToken = params[2];
        }
        else {
            jwtToken = null;
        }

        String result = null;

        Request request = prepareRequest();

        com.squareup.okhttp.Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response.isSuccessful()) {
            try {
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            return null;
        }

        return prepareResponse(result);
    }

    private Object prepareResponse(String result) {
        if (httpRequestAndResponseType == HttpRequestAndResponseType.LOGIN) {
            return JsonLoader.convertJsonToTokenDTO(result);
        }

        return null;
    }


}
