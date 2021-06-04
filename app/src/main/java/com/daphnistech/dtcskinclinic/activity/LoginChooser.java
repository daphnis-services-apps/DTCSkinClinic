package com.daphnistech.dtcskinclinic.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;

public class LoginChooser extends AppCompatActivity implements View.OnClickListener {
    private TextView doctorLogin, patientLogin;
    private Context context;
    private PreferenceManager preferenceManager;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_chooser);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }
        initViews();

        context = LoginChooser.this;
        preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);

        doctorLogin.setOnClickListener(this);
        patientLogin.setOnClickListener(this);
    }

    private void initViews() {
        doctorLogin = findViewById(R.id.doctorLogin);
        patientLogin = findViewById(R.id.patientLogin);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(context, LoginActivity.class);
        if (v == doctorLogin) {
            preferenceManager.setLoginType(Constant.DOCTOR);
        } else if (v == patientLogin) {
            preferenceManager.setLoginType(Constant.PATIENT);
        }
        startActivity(intent);
        finish();
    }
}