package com.rmj.parking_place.utils;

import com.rmj.parking_place.model.Zone;
import java.util.List;

public interface AsyncResponse {
    void processFinish(Response response);

    void loginAgain();
}
