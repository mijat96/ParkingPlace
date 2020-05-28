package com.rmj.parking_place.dialogs;

import android.content.DialogInterface;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

import com.rmj.parking_place.fragments.MapPageFragment;
import com.rmj.parking_place.model.TicketType;

public class DialogForSelectingTicketType {
    private boolean onlyRegularTicket;
    private MapPageFragment mapPageFragment;
    private AlertDialog dialog;


    public DialogForSelectingTicketType(boolean onlyRegularTicket, MapPageFragment mapPageFragment) {
        this.onlyRegularTicket = onlyRegularTicket;
        this.mapPageFragment = mapPageFragment;
    }

    public void showDialog() {
        String[] ticketTypes = getTicketTypes(onlyRegularTicket);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mapPageFragment.getActivity());
        alertDialogBuilder.setTitle("Select a ticket type");
        alertDialogBuilder.setSingleChoiceItems(ticketTypes, 0, (dialog, item) -> { })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ListView lw = ((AlertDialog)dialog).getListView();
                        String selectedTicketTypeStr = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
                        TicketType selectedTicketType = TicketType.valueOf(selectedTicketTypeStr);
                        mapPageFragment.continueWithTakingOfParkingPlace(selectedTicketType);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public boolean isOnlyRegularTicket() {
        return onlyRegularTicket;
    }

    public String[] getTicketTypes(boolean onlyRegularTicket) {
        if (onlyRegularTicket) {
            return new String[] { TicketType.REGULAR.name() };
        }
        else {
            return new String[] { TicketType.REGULAR.name(), TicketType.MONTHLY.name(), TicketType.YEARLY.name() };
        }
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public void cancel() {
        dialog.cancel();
    }

}
