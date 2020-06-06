package com.rmj.parking_place.actvities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportIlegalyParkedActivity extends AppCompatActivity {

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
        if (requestCode == CAMERA_PIC_REQUEST) {
            image = (Bitmap) data.getExtras().get("data");
            ImageView imageview = (ImageView) findViewById(R.id.show_captured_image); //sets imageview as the bitmap
            imageview.setImageBitmap(image);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    private void sendReport() {
        File f = createFile(selectedParkingPlace.getId().toString());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        FileOutputStream fos = outputStream(bitmapdata, f);

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), f);
        MultipartBody.Part body = MultipartBody.Part.createFormData("upload", f.getName(), reqFile);

        Context context = this;
        ParkingPlaceServerUtils.reportService.reportParkedCar(body).
            enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        new AlertDialog.Builder(context)
                                .setTitle("Report successfully send")
                                .setMessage("Report successfully send")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        finish();
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    //failure message
                    t.printStackTrace();
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

    private FileOutputStream outputStream(byte[] bitmapdata, File f) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (fos != null) {
            return fos;
        }
        return null;
    }
}
