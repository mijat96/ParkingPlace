package com.rmj.parking_place.service;

import com.rmj.parking_place.App;
import com.rmj.parking_place.utils.TokenUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ParkingPlaceServerUtils {

    private static long CONNECTION_TIMEOUT = 120; // sec
    private static long READ_TIMEOUT = 120; // sec
    private static long WRITE_TIMEOUT = 120; // sec

    public static OkHttpClient getClient(){
        //HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        //interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                //.addInterceptor(interceptor)
                // TODO add intercept for jwt token
                .build();

        return client;
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustManagers = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            Interceptor tokenInterceptor = new Interceptor() {
                @NotNull
                @Override
                public Response intercept(@NotNull Chain chain) throws IOException {
                    Request request = chain.request();
                    String jwtToken = TokenUtils.getToken();
                    Request newRequest = request.newBuilder()
                            .addHeader("token", jwtToken)
                            .build();
                    Response response = chain.proceed(newRequest);
                    if (response.code() == 401) {
                        App.loginAgain();
                    }
                    return response;
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManagers[0])
                    .hostnameVerifier(hostnameVerifier)
                    .addInterceptor(tokenInterceptor)
                    .build();

            return client;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(App.getParkingPlaceServerUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .client(getUnsafeOkHttpClient())
            .build();


    public static AuthenticationService authenticationService = retrofit.create(AuthenticationService.class);
    public static UserService userService = retrofit.create(UserService.class);
    public static ParkingPlaceService parkingPlaceService = retrofit.create(ParkingPlaceService.class);
    public static ZoneService zoneService = retrofit.create(ZoneService.class);
}
