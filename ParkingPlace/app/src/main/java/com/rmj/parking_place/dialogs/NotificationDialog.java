package com.rmj.parking_place.dialogs;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.rmj.parking_place.App;

public class NotificationDialog {
    private AlertDialog dialog;
    private String message;
    private final String buttonText = "Close";
    private Context context;

    public NotificationDialog(String message, Context context) {
        this.message = message;
        this.context = context;
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                // .setPositiveButton()
                .setNeutralButton(buttonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
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
