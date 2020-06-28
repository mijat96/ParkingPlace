package com.rmj.parking_place.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rmj.parking_place.R;
import com.rmj.parking_place.database.NotificationDb;
import com.rmj.parking_place.database.NotificationRepository;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.service.ParkingPlaceServerUtils;
import com.rmj.parking_place.utils.NotificationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TakeAndReserveReceiver extends BroadcastReceiver {
    private static NotificationRepository notificationRepository;
    public static String NOTIFICATION_ID = "notification_id";
    public static String PARKING_PLACE_ID = "parking_place_id";
    public ParkingPlace place;

    @Override
    public void onReceive(Context context, Intent intent) {
        notificationRepository = new NotificationRepository(context);
        int paringPlaceId = intent.getIntExtra(PARKING_PLACE_ID, 0);
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        Toast.makeText(context, "Again take, id:" + paringPlaceId , Toast.LENGTH_SHORT).show();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                //kontaktiranje api-ja za produzenje zauzeca
                getParkingPlace(paringPlaceId, notificationId);
                return null;
            }
        }.execute();
        updateRemainingTime(7200000, context);
    }

    public void updateRemainingTime(long remainingTimeInSeconds, Context context) {
        long hours = remainingTimeInSeconds / 3600;
        long differenceInSecondsWithoutHours = remainingTimeInSeconds - hours * 3600;
        long mins = differenceInSecondsWithoutHours / 60;
        long secs = differenceInSecondsWithoutHours % 60;

        View view = View.inflate(context, R.layout.fragment_map_page, null);

        final String remainingTimeStr = "Remaining time:  " + prepareRemainingTimeString(hours, mins, secs);
        ((TextView) view.findViewById(R.id.txtRemainingTime)).setText(remainingTimeStr);
        view.refreshDrawableState();
    }

    private String prepareRemainingTimeString(long hours, long mins, long secs) {
        String remainingTimeString = "";

        if (hours / 10 < 1) {
            remainingTimeString += "0" + hours;
        }
        else {
            remainingTimeString += hours;
        }
        remainingTimeString += ":";

        if (mins / 10 < 1) {
            remainingTimeString += "0" + mins;
        }
        else {
            remainingTimeString += mins;
        }
        remainingTimeString += ":";

        if (secs / 10 < 1) {
            remainingTimeString += "0" + secs;
        }
        else {
            remainingTimeString += secs;
        }

        return remainingTimeString;
    }


    private void againTakeParkingPlace(long paringPlaceId) {
        ParkingPlaceServerUtils.parkingPlaceService.againTakeParkinPlace(paringPlaceId)
                .enqueue(new Callback<ParkingPlace>() {
                    @Override
                    public void onResponse(Call<ParkingPlace> call, Response<ParkingPlace> response) {
                        if (response == null || (response != null && !response.isSuccessful())) {

                        }else {
                            //setupNotification(paringPlaceId);
                        }
                    }

                    @Override
                    public void onFailure(Call<ParkingPlace> call, Throwable t) {

                    }
                });
    }

    private void getParkingPlace(long parkingPlaceId, long notificationId) {
        ParkingPlaceServerUtils.parkingPlaceService.getParkingPlace(parkingPlaceId)
                .enqueue(new Callback<ParkingPlace>() {
                    @Override
                    public void onResponse(Call<ParkingPlace> call, Response<ParkingPlace> response) {
                        if (response == null || (response != null && !response.isSuccessful())) {

                        }else {
                            setupNotification(notificationId);
                        }
                    }

                    @Override
                    public void onFailure(Call<ParkingPlace> call, Throwable t) {

                    }
                });
    }

    private void setupNotification(long reservation) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                String notificationTitle;
                String notificationText;
                String notificationType;
                long notificationId;
                long dateTime;
                notificationTitle = "Taking expire";
                notificationText = "Soon occupied parking space expires";
                notificationType = "taking_notification";
                notificationId = reservation;
                dateTime = System.currentTimeMillis() + 30*1000;


                NotificationDb notificationDb = new NotificationDb(notificationId, notificationTitle, notificationText,
                        notificationType, dateTime, reservation);
                NotificationUtils.scheduleNotification(notificationDb);
                notificationRepository.insertNotification(notificationDb);

                return null;
            }
        }.execute();
    }

    //RESERVATION
    /*@Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        int paringPlaceId = intent.getIntExtra(PARKING_PLACE_ID, 0);
        Toast.makeText(context, "Akcija notifikacije, id:" , Toast.LENGTH_SHORT).show();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                getParkingPlace(paringPlaceId);
                return null;
            }
        }.execute();
    }

    private void getParkingPlace(long parkingPlaceId) {
        ParkingPlaceServerUtils.parkingPlaceService.getParkingPlace(parkingPlaceId)
                .enqueue(new Callback<ParkingPlace>() {
                    @Override
                    public void onResponse(Call<ParkingPlace> call, Response<ParkingPlace> response) {
                        if (response == null || (response != null && !response.isSuccessful())) {

                        }else {
                            place = response.body();
                            ReservingDTO dto = new ReservingDTO(place.getZone().getId(), place.getId(),
                                    place.getLocation().getLatitude(), place.getLocation().getLongitude());
                            reserveParkingPlaceOnServer(dto, place.getZone());
                        }
                    }

                    @Override
                    public void onFailure(Call<ParkingPlace> call, Throwable t) {

                    }
                });
    }

    private void reserveParkingPlaceOnServer(ReservingDTO dto, Zone zone) {
        ParkingPlaceServerUtils.parkingPlaceService.reserveParkingPlace(dto)
                .enqueue(new Callback<ReservationDTO>() {
                    @Override
                    public void onResponse(Call<ReservationDTO> call, retrofit2.Response<ReservationDTO> response) {
                        if (response == null || (response != null && !response.isSuccessful())) {

                        }else {
                            ReservationDTO reservationDTO = response.body();
                            ParkingPlace parkingPlace = reservationDTO.getParkingPlace();
                            parkingPlace.setZone(zone);
                            Reservation reservation = new Reservation(reservationDTO.getId(), reservationDTO.getStartDateTimeAndroid(),
                                    reservationDTO.getStartDateTimeServer(), parkingPlace);
                            setupNotification(reservation);
                        }
                    }

                    @Override
                    public void onFailure(Call<ReservationDTO> call, Throwable t) {
                        //Toast.makeText(mainActivity, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void setupNotification(Reservation reservation) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                String notificationTitle;
                String notificationText;
                String notificationType;
                long notificationId;
                long dateTime;
                    notificationTitle = "Remaining time for reservation of parking place:  1 min";
                    notificationText = "Soon expire reservation of parking place";
                    notificationType = "reseravation_notification";
                    notificationId = reservation.getId();
                    dateTime = System.currentTimeMillis() + 60000;


                NotificationDb notificationDb = new NotificationDb(notificationId, notificationTitle, notificationText,
                        notificationType, dateTime, reservation.getParkingPlace().getId());
                NotificationUtils.scheduleNotification(notificationDb);
                notificationRepository.insertNotification(notificationDb);

                return null;
            }
        }.execute();
    }*/
}
