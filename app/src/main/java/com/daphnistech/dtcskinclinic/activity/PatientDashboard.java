package com.daphnistech.dtcskinclinic.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.account.MyAccount;
import com.daphnistech.dtcskinclinic.chat.ChatList;
import com.daphnistech.dtcskinclinic.firebase.NotificationUtils;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.daphnistech.dtcskinclinic.home.Home;
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

public class PatientDashboard extends AppCompatActivity {
    public ChipNavigationBar chipNavigationBar;
    private Context context;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }

        initViews();
        preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);
        FirebaseMessaging.getInstance().subscribeToTopic(Constant.PATIENT);


        //chipNavigationBar.showBadge(R.id.chat, 5);
        if (preferenceManager.isFirstTimeLogin()) {
            preferenceManager.setFirstTimeLogin(false);
            chipNavigationBar.setItemSelected(R.id.home, true);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new Home()).commit();
        } else {
            chipNavigationBar.setItemSelected(R.id.chat, true);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChatList()).commit();
        }

        chipNavigationBar.setOnItemSelectedListener(i -> {
            Fragment fragment = null;
            if (i == R.id.home) {
                fragment = new Home();
            } else if (i == R.id.chat) {
                fragment = new ChatList();
            } else if (i == R.id.account) {
                fragment = new MyAccount();
            }
            assert fragment != null;
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();
        });

    }


    private void initViews() {
        context = PatientDashboard.this;
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
                        }
                        //Parsing the JSON

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");

                    }
                } else if (response.errorBody() != null) {

                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {

            }
        });
    }

    @Override
    protected void onPause() {
        if (NotificationUtils.isAppIsInBackground(context))
            putStatus(Constant.OFFLINE);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (!NotificationUtils.isAppIsInBackground(context))
            putStatus(Constant.ONLINE);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Dialog dialog = new Dialog(context);
        // Removing the features of Normal Dialogs
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_exit);
        dialog.setCancelable(true);
        dialog.show();

        dialog.findViewById(R.id.confirm).setOnClickListener(confirm -> finish());
        dialog.findViewById(R.id.cancel).setOnClickListener(cancel -> dialog.dismiss());
    }
}