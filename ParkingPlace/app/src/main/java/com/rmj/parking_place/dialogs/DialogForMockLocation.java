package com.rmj.parking_place.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.rmj.parking_place.actvities.MainActivity;

public class DialogForMockLocation {
    private AlertDialog dialog;
    private final String message = "If you want to continue using this application,"
                                     + " you must turn off the ALLOW_MOCK_LOCATION option.";
    private final String buttonExitText = "Exit";
    private final String neutralButtonText = "Go to Developer Options";
    private Activity activity;

    public DialogForMockLocation(Activity activity) {
        this.activity = activity;
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setCancelable(false)
                // .setPositiveButton()
                .setNegativeButton(buttonExitText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        // exit from app
                        activity.finish();
                        System.exit(0);

                    }
                })
                .setNeutralButton(neutralButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        goToDeveloperOptions();
                        dialog.cancel();
                    }
                });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void goToDeveloperOptions() {
        activity.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
    }

    public void cancel() {
        dialog.cancel();
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public String getMessage() {
        return message;
    }

}
