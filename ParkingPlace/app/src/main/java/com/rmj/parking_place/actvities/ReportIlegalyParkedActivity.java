package com.rmj.parking_place.actvities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.rmj.parking_place.R;
import com.rmj.parking_place.dto.DTO;
import com.rmj.parking_place.dto.ReportDTO;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.ReportTypes;
import com.rmj.parking_place.service.ParkingPlaceServerUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportIlegalyParkedActivity extends AppCompatActivity {

    public static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 99;
    private Bitmap image;
    static final int CAMERA_PIC_REQUEST = 1337;
    private ParkingPlace selectedParkingPlace = null;
    private static String[] reportTypes = {
            ReportTypes.BAD_PARKED.name(),
            ReportTypes.TAKE_RESERVED.name(),
            ReportTypes.NO_TAKE.name()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_ilegaly_parked_layout);

        Intent intent = getIntent();

        if (savedInstanceState == null) {
            selectedParkingPlace = (ParkingPlace) intent.getParcelableExtra("selected_parking_place");
        } else {
            selectedParkingPlace = savedInstanceState.getParcelable("selected_parking_place");
        }

        Button openCameraButton = (Button) findViewById(R.id.open_camera);

        openCameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openCamera();
            }
        });

        Button sendReportButton = (Button) findViewById(R.id.send_report_button);

        sendReportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendReport();
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.reportTypeSpinner);
        ArrayAdapter<CharSequence> adapter =
                new ArrayAdapter<CharSequence>(this,
                        R.layout.support_simple_spinner_dropdown_item, android.R.id.text1, reportTypes);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            image = (Bitmap) data.getExtras().get("data");
            ImageView imageview = (ImageView) findViewById(R.id.show_captured_image); //sets imageview as the bitmap
            imageview.setImageBitmap(image);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    private void sendReport() {
        if (image == null || (image != null && image.getByteCount() == 0)) {
            Toast.makeText(this, "You did not select an image!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fname = selectedParkingPlace.getId().toString() + "-" + Calendar.getInstance().getTime().toString() + ".jpg";

        File f = createFile(fname);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        saveImage(image, fname);
        String reason = getSpinnerReason();
        RequestBody req = RequestBody.create(bitmapdata);
        MultipartBody.Part bodyFile = MultipartBody.Part.createFormData("upload", fname, req);

        MultipartBody.Part bodyParkingId = MultipartBody.Part.createFormData("parkingPlaceId", selectedParkingPlace.getId().toString());
        MultipartBody.Part bodyZoneId = MultipartBody.Part.createFormData("zoneId", selectedParkingPlace.getZone().getId().toString());
        MultipartBody.Part bodyReasonReport = MultipartBody.Part.createFormData("reason", reason);

        Context context = this;
        ParkingPlaceServerUtils.reportService.reportParkedCar(bodyFile, bodyParkingId, bodyZoneId, bodyReasonReport).
            enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        new AlertDialog.Builder(context)
                                .setTitle("Report successfully send")
                                .setMessage("Report successfully send")
                                .setIcon(R.drawable.icon_success)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                        finish();
                                    }})
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    new AlertDialog.Builder(context)
                            .setTitle("Report failute send")
                            .setMessage("Report failute send: " + t.getMessage())
                            .setIcon(R.drawable.icon_failure)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }})
                            .show();
                }
            });
    }

    private File createFile(String fileName) {
        File f = new File(this.getCacheDir(), fileName);
        try {
            f.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }

    private void saveImage(Bitmap finalBitmap, String image_name) {

        if(!checkEWritexternalStoragePermission()){
            return;
        }

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + image_name;
        File file = new File(myDir, fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkEWritexternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    new AlertDialog.Builder(this)
                            .setTitle("Allow write external storage")
                            .setMessage("To continue working we need write external storage....Allow now?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                                }
                            })
                            .create()
                            .show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private String getSpinnerReason(){
        Spinner spinner = (Spinner) findViewById(R.id.reportTypeSpinner);
        return  spinner.getSelectedItem().toString();
    }

}
