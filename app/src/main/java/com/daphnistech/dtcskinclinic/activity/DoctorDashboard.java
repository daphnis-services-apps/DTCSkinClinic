package com.daphnistech.dtcskinclinic.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.account.MyAccount;
import com.daphnistech.dtcskinclinic.appointment.MyAppointments;
import com.daphnistech.dtcskinclinic.chat.ChatList;
import com.daphnistech.dtcskinclinic.firebase.AppointmentIntentService;
import com.daphnistech.dtcskinclinic.firebase.NotificationUtils;
import com.daphnistech.dtcskinclinic.helper.APIManager;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class DoctorDashboard extends AppCompatActivity {
    public ChipNavigationBar chipNavigationBar;
    Context context;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }

        initViews();

        preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);
        FirebaseMessaging.getInstance().subscribeToTopic(Constant.DOCTOR);

        chipNavigationBar.setItemSelected(R.id.account, true);
        //chipNavigationBar.showBadge(R.id.chat, 5);
        AppointmentIntentService.handleActionDismiss(context);
        if (getIntent().getBooleanExtra("push", false))
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MyAppointments()).commit();
        else if (getIntent().getBooleanExtra("conversation", false)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChatList()).commit();
            chipNavigationBar.setItemSelected(R.id.chat, true);
        } else
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MyAccount()).commit();

        chipNavigationBar.setOnItemSelectedListener(i -> {
            Fragment fragment = null;
            if (i == R.id.chat) {
                fragment = new ChatList();
            } else if (i == R.id.account) {
                fragment = new MyAccount();
            }
            assert fragment != null;
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();
        });
    }

    private void initViews() {
        context = DoctorDashboard.this;
        chipNavigationBar = findViewById(R.id.bottomMenu);
    }

    private void putStatus(boolean status) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call = api.putStatus(preferenceManager.getUserID(), preferenceManager.getLoginType(), String.valueOf(status));

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (jsonObject.getBoolean("error")) {
                                Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                        //Parsing the JSON

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(context, "Returned empty response", Toast.LENGTH_SHORT).show();

                    }
                } else if (response.errorBody() != null) {
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        if (NotificationUtils.isAppIsInBackground(context)) {
        }
        APIManager.getInstance().putStatus(context, Constant.OFFLINE);
        //putStatus(Constant.OFFLINE);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (NotificationUtils.isAppIsInBackground(context))
            APIManager.getInstance().putStatus(context, Constant.ONLINE);
        putStatus(Constant.ONLINE);
        super.onResume();
    }

    public void setBottomSelected() {
        chipNavigationBar.setItemSelected(R.id.chat, true);
    }
}
