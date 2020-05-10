package com.rmj.parking_place.actvities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.Toast;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;

public class CheckWifiActivity extends AppCompatActivity {

    private Timer timer;
    private WifiManager wifiManager;

    private static final int  NUMBER_OF_LEVELS = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        startTimerForCheckingWIfiSignal();
    }

    private void startTimerForCheckingWIfiSignal() {
        final CheckWifiActivity that = this;

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final String levelStr = getNetworkSignalLevel();
                if (!levelStr.equals("Moderate (3)") && !levelStr.equals("Good (4)") && !levelStr.equals("Excellent (5)")) {
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(that, "Wifi signal level:  " + levelStr, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }, 0, 10000);//put here time 10000 milliseconds=10 seconds
    }

    private String getNetworkSignalLevel() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), NUMBER_OF_LEVELS);
        String levelStr;

        switch (level)
        {
            case 0:
                levelStr = "None (0)";
                break;

            case 1:
                levelStr = "None (1)";
                break;

            case 2:
                levelStr = "Poor (2)";
                break;

            case 3:
                levelStr = "Moderate (3)";
                break;

            case 4:
                levelStr = "Good (4)";
                break;

            case 5:
                levelStr = "Excellent (5)";
                break;
            default:
                levelStr = "Error (-1)";
        }

        return levelStr;
    }

    private void stopTimer() {
        timer.cancel();
        timer.purge();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
    }

}
