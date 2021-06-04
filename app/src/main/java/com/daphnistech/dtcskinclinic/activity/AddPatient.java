package com.daphnistech.dtcskinclinic.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.patient.CurrentDiseasesDetails;
import com.daphnistech.dtcskinclinic.patient.PatientDetails;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class AddPatient extends AppCompatActivity {
    PreferenceManager preferenceManager;
    Context context;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }
        context = AddPatient.this;
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new PatientDetails()).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            if (fragment instanceof PatientDetails) {
                PatientDetails patientDetails = (PatientDetails) fragment;
                patientDetails.profilePic.setImageURI(data.getData());
                String path = context.getApplicationContext().getExternalFilesDir("Me/Profile").getAbsolutePath();
                try {
                    FileOutputStream mFileOutStream = new FileOutputStream(path + "/pic" + ".PNG");
                    ((BitmapDrawable) (patientDetails.profilePic.getDrawable())).getBitmap().compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                    new PreferenceManager(context , Constant.USER_DETAILS).setProfileImage(path + "/pic" + ".PNG");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                CurrentDiseasesDetails currentDiseasesDetails = (CurrentDiseasesDetails) fragment;
                preferenceManager = new PreferenceManager(context, currentDiseasesDetails.diseaseName);
                if (currentDiseasesDetails.isPdfChoose) {
                    currentDiseasesDetails.pdfName.setText(data.getData().getPath());
                    currentDiseasesDetails.isPdfChoose = false;
                    preferenceManager.setPDF(data.getData().getPath());
                } else {
                    Uri fileUri = data.getData();
                    if (preferenceManager.getSteps() == 3) {
                        preferenceManager.setSteps(0);
                    }
                    if (preferenceManager.getSteps() == 0) {
                        currentDiseasesDetails.pic1.setImageURI(fileUri);
                        currentDiseasesDetails.pic1Layout.setVisibility(View.VISIBLE);
                        currentDiseasesDetails.imageLayout.setVisibility(View.VISIBLE);
                        preferenceManager.setPic1(fileUri.toString());
                        preferenceManager.setPic1Path(data.getData().getPath());
                        preferenceManager.setSteps(1);
                    } else if (preferenceManager.getSteps() == 1) {
                        currentDiseasesDetails.pic1.setImageURI(Uri.parse(preferenceManager.getPic1()));
                        currentDiseasesDetails.imageLayout.setVisibility(View.VISIBLE);
                        currentDiseasesDetails.pic1Layout.setVisibility(View.VISIBLE);
                        currentDiseasesDetails.pic2.setImageURI(fileUri);
                        currentDiseasesDetails.pic2Layout.setVisibility(View.VISIBLE);
                        preferenceManager.setPic2(fileUri.toString());
                        preferenceManager.setPic2Path(data.getData().getPath());
                        preferenceManager.setSteps(2);
                    } else if (preferenceManager.getSteps() == 2) {
                        currentDiseasesDetails.pic1.setImageURI(Uri.parse(preferenceManager.getPic1()));
                        currentDiseasesDetails.imageLayout.setVisibility(View.VISIBLE);
                        currentDiseasesDetails.pic1Layout.setVisibility(View.VISIBLE);
                        currentDiseasesDetails.pic2.setImageURI(Uri.parse(preferenceManager.getPic2()));
                        currentDiseasesDetails.pic2Layout.setVisibility(View.VISIBLE);
                        currentDiseasesDetails.pic3.setImageURI(fileUri);
                        currentDiseasesDetails.pic3Layout.setVisibility(View.VISIBLE);
                        currentDiseasesDetails.uploadCardView.setVisibility(View.GONE);
                        preferenceManager.setPic3(fileUri.toString());
                        preferenceManager.setPic3Path(data.getData().getPath());
                        preferenceManager.setSteps(3);
                    }
                }
            }
        }
    }
}