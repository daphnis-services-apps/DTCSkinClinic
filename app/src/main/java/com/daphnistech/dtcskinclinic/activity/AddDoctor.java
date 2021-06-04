package com.daphnistech.dtcskinclinic.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.doctor.DoctorDetails;

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
}
