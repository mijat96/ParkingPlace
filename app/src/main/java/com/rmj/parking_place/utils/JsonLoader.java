package com.rmj.parking_place.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rmj.parking_place.model.Zone;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class JsonLoader {

    private static Gson gson = new Gson();
    private static final int bufferSize = 1024;

    public static List<Zone> getZones(InputStream is) {
        String json = loadJsonZones(is);
        // System.out.println("json = " + json);
        List<Zone> zones = gson.fromJson(json, new TypeToken<List<Zone>>(){}.getType());
        return zones;
    }

    private static char[] subArray(char[] array, int begin, int size) {
        return Arrays.copyOfRange(array, begin, begin + size);
    }

    private static String loadJsonZones(InputStream is) {
        InputStreamReader reader = new InputStreamReader(is, Charset.forName("UTF-8"));

        char[] buffer = new char[bufferSize];
        StringBuilder sb = new StringBuilder("");
        int currentBufferSize;

        try {
            while ( (currentBufferSize = reader.read(buffer)) == bufferSize ) {
                sb.append(buffer);
            }
            char[] lastBlock = subArray(buffer, 0, currentBufferSize);
            sb.append(lastBlock);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String json = sb.toString();
        return json;
    }

    private static List<Zone> convertJsonToZones(String json) {
        List<Zone> zones = gson.fromJson(json, new TypeToken<List<Zone>>(){}.getType());
        return zones;
    }
}
