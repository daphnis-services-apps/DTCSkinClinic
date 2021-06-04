package com.daphnistech.dtcskinclinic.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.MyApplication;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.daphnistech.dtcskinclinic.welcome.FirstScreen;
import com.daphnistech.dtcskinclinic.welcome.SecondScreen;
import com.daphnistech.dtcskinclinic.welcome.ThirdScreen;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private Context context;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(Color.WHITE);
        }
        context = MainActivity.this;
        MyApplication.getInstance().getPrefManager().clearNotifications();
        getApplicationContext().getExternalFilesDir(getResources().getString(R.string.app_name));
        //downloadImage();
        if (new PreferenceManager(context, Constant.USER_DETAILS).isLoggedIn()) {
            if (new PreferenceManager(context, Constant.USER_DETAILS).getLoginType().equals(Constant.PATIENT)) {
                startActivity(new Intent(context, PatientDashboard.class));
            } else {
                startActivity(new Intent(context, DoctorDashboard.class));
            }
            finish();
        } else if (new PreferenceManager(context,Constant.USER_DETAILS).isVisited()){
            startActivity(new Intent(context, LoginChooser.class));
            finish();
        }
        //PreferenceManager.clearAll(context);
        TextView next = findViewById(R.id.next);
        viewPager = findViewById(R.id.viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        CircleIndicator indicator = findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);

        next.setOnClickListener(v -> {
            int position = viewPager.getCurrentItem();
            if (position < 2) {
                viewPager.setCurrentItem(position + 1);
            } else {
                new PreferenceManager(context,Constant.USER_DETAILS).setVisited(true);
                startActivity(new Intent(context, LoginChooser.class));
                finish();
            }
        });
    }

    private void downloadImage() {
        CustomProgressBar.showProgressBar(context, false);
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://dtcskinclinic.globalexpomart.com/v1/messages/309488-img_20210508_193420076.jpg")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                InputStream inputStream = response.body().byteStream();
                try {
                    ContentResolver resolver = context.getContentResolver();
                    ContentValues values = new ContentValues();
                    // save to a folder
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, "DTCSkinClinic_" + (new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date())) + ".PNG");
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "image/*");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);
                    // You can use this outputStream to write whatever file you want:
                    OutputStream outputStream = resolver.openOutputStream(uri);
                    int read;
                    byte[] bytes = new byte[8129];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    CustomProgressBar.hideProgressBar();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    Toast.makeText(MainActivity.this, e1.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dtcskinclinic.globalexpomart.com/v1/messages/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call = api.getImage();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        try {
                            JSONObject jsonObject = new JSONObject();
                            if (!jsonObject.getBoolean("error")) {
                                CustomProgressBar.hideProgressBar();
                            } else {
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

    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        @SuppressWarnings("deprecation")
        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                fragment = new FirstScreen();
            } else if (position == 1) {
                fragment = new SecondScreen();
            } else if (position == 2) {
                fragment = new ThirdScreen();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}