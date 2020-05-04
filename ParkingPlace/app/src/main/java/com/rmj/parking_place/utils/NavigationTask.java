package com.rmj.parking_place.utils;

import com.rmj.parking_place.model.FromTo;

public class NavigationTask extends GetRequestAsyncTask {

    private FromTo fromTo;

    public NavigationTask(AsyncResponse delegate, FromTo fromTo) {
        super(delegate);
        this.fromTo = fromTo;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..

        delegate.processFinish(new NavigationResponse(httpRequestAndResponseType, result, fromTo));
    }
}
