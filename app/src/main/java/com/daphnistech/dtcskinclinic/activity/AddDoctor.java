package com.daphnistech.dtcskinclinic.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.doctor.DoctorDetails;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class AddDoctor extends AppCompatActivity {
    Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_doctor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }
        context = AddDoctor.this;
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new DoctorDetails()).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            DoctorDetails doctorDetails = (DoctorDetails) fragment;
            doctorDetails.profilePic.setImageURI(data.getData());
            String path = context.getApplicationContext().getExternalFilesDir("Me/Profile").getAbsolutePath();
            try {
                FileOutputStream mFileOutStream = new FileOutputStream(path + "/pic" + ".PNG");
                ((BitmapDrawable) (doctorDetails.profilePic.getDrawable())).getBitmap().compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                new PreferenceManager(context, Constant.USER_DETAILS).setProfileImage(path + "/pic" + ".PNG");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
