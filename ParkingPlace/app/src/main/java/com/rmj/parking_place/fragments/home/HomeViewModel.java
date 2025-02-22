package com.rmj.parking_place.fragments.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rmj.parking_place.actvities.MainActivity;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void changeFragmentInMainActivity(MainActivity mainActivity, int buttonId) {
        mainActivity.selectDrawerItem(buttonId);
    }
}