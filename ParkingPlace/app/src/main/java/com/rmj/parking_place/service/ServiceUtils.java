package com.rmj.parking_place.service;

import android.content.Context;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;
// import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by skapl on 09-May-17.
 */
public class ServiceUtils {

	//EXAMPLE: http://192.168.43.73:8080/rs.ftn.reviewer.rest/rest/proizvodi/
    public static final String SERVICE_API_PATH = "http://<service_ip_adress>:<service_port>/rs.ftn.reviewer.rest/rest/proizvodi/";
    public static final String ADD = "add";

    public static OkHttpClient test(){
        //HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        //interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                //.addInterceptor(interceptor)
                // TODO add intercept for jwt token
                .build();

        return client;
    }

    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(SERVICE_API_PATH)
            .addConverterFactory(GsonConverterFactory.create())
            .client(test())
            .build();


    public static ReviewerService reviewerService = retrofit.create(ReviewerService.class);
}
