package com.daphnistech.dtcskinclinic.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class PatientEditCurrentDetails extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    private Bitmap bitmap1, bitmap2, bitmap3;
    private ProgressBar pic1ProgressBar, pic2ProgressBar, pic3ProgressBar;
    private CardView pic1Layout, pic2Layout, pic3Layout;
    private ImageView pic1, pic2, pic3, image;
    private String diseaseName;
    private TextView pdfName;
    private TextView heading ,heading1;
    private TextView oldAge, comments, diseaseType, problem;
    private PreferenceManager preferenceManager;
    private View headerView;
    private RelativeLayout problemsLayout, imageView;
    private LinearLayout imageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_edit_current_details);
        initViews();
        CustomProgressBar.showProgressBar(context, false);
        new Handler().postDelayed(this::settingValues, 100);
    }

    private void initViews() {
        context = PatientEditCurrentDetails.this;
        pic1Layout = findViewById(R.id.pic1Layout);
        pic1ProgressBar = findViewById(R.id.pic1ProgressBar);
        pic2Layout = findViewById(R.id.pic2Layout);
        pic2ProgressBar = findViewById(R.id.pic2ProgressBar);
        pic3Layout = findViewById(R.id.pic3Layout);
        pic3ProgressBar = findViewById(R.id.pic3ProgressBar);
        pic1 = findViewById(R.id.pic1);
        pic2 = findViewById(R.id.pic2);
        pic3 = findViewById(R.id.pic3);
        image = findViewById(R.id.image);
        imageView = findViewById(R.id.imageView);
        pdfName = findViewById(R.id.pdfName);
        heading = findViewById(R.id.text);
        heading1 = findViewById(R.id.text1);
        diseaseType = findViewById(R.id.diseaseType);
        problem = findViewById(R.id.problems);
        oldAge = findViewById(R.id.oldage);
        comments = findViewById(R.id.comments);
        headerView = findViewById(R.id.headerView);
        problemsLayout = findViewById(R.id.problemLayout);
        imageLayout = findViewById(R.id.imageLayout);
        diseaseName = getIntent().getStringExtra("type");
        preferenceManager = new PreferenceManager(this,diseaseName);
    }

    private void settingValues() {
        if (!preferenceManager.getPic1().equals("N/A")) {
            preferenceManager.setSteps(1);
            pic1Layout.setVisibility(View.VISIBLE);
            imageLayout.setVisibility(View.VISIBLE);
            getBitmapFromURL(new OnCalLBack() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    runOnUiThread(() -> {
                        pic1.setImageBitmap(bitmap);
                        pic1ProgressBar.setVisibility(View.GONE);
                    });
                    bitmap1 = bitmap;
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, preferenceManager.getPic1());
            pic1.setOnClickListener(this);
        }
        if (!preferenceManager.getPic2().equals("N/A")) {
            preferenceManager.setSteps(2);
            pic2Layout.setVisibility(View.VISIBLE);
            imageLayout.setVisibility(View.VISIBLE);
            getBitmapFromURL(new OnCalLBack() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    runOnUiThread(() -> {
                        pic2.setImageBitmap(bitmap);
                        pic2ProgressBar.setVisibility(View.GONE);
                    });
                    bitmap2 = bitmap;
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, preferenceManager.getPic2());
            pic2.setOnClickListener(this);
        }
        if (!preferenceManager.getPic3().equals("N/A")) {
            preferenceManager.setSteps(3);
            pic3Layout.setVisibility(View.VISIBLE);
            imageLayout.setVisibility(View.VISIBLE);
            getBitmapFromURL(new OnCalLBack() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    runOnUiThread(() -> {
                        pic3.setImageBitmap(bitmap);
                        pic3ProgressBar.setVisibility(View.GONE);
                    });
                    bitmap3 = bitmap;

                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, preferenceManager.getPic3());
            pic3.setOnClickListener(this);
        }

        CustomProgressBar.hideProgressBar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (diseaseName.equals(Constant.SKIN_DISEASE)) {
                heading1.setTextColor(getResources().getColor(R.color.loginChooser));
                window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
                headerView.setBackground(getDrawable(R.drawable.dashboard_header));
                imageView.setBackground(getDrawable(R.drawable.button_custom));
                image.setImageDrawable(getDrawable(R.drawable.skin_disease));
            } else if (diseaseName.equals(Constant.SEX_DISEASE)) {
                heading1.setTextColor(getResources().getColor(R.color.pink));
                window.setStatusBarColor(getResources().getColor(R.color.pink));
                headerView.setBackground(getDrawable(R.drawable.pink_header));
                imageView.setBackground(getDrawable(R.drawable.pink_button_custom));
                image.setImageDrawable(getDrawable(R.drawable.sex_disease));
            } else if (diseaseName.equals(Constant.HAIR_PROBLEM)) {
                heading1.setTextColor(getResources().getColor(R.color.light_black));
                window.setStatusBarColor(getResources().getColor(R.color.light_black));
                headerView.setBackground(getDrawable(R.drawable.light_black_header));
                imageView.setBackground(getDrawable(R.drawable.light_black_button_custom));
                image.setImageDrawable(getDrawable(R.drawable.hair_problem));
            } else if (diseaseName.equals(Constant.COSMETOLOGY)) {
                heading1.setTextColor(getResources().getColor(R.color.palm));
                window.setStatusBarColor(getResources().getColor(R.color.palm));
                headerView.setBackground(getDrawable(R.drawable.palm_header));
                imageView.setBackground(getDrawable(R.drawable.palm_button_custom));
                image.setImageDrawable(getDrawable(R.drawable.cosmetology));
            }
        }

        heading.setText(String.format("%s Info", diseaseName));
        heading1.setText(String.format("%s Info", diseaseName));
        oldAge.setText(preferenceManager.getOldAge());
        comments.setText(preferenceManager.getComments());
        diseaseType.setText(preferenceManager.getDiseaseType());

        if (diseaseName.equals(Constant.SEX_DISEASE))
            problemsLayout.setVisibility(View.GONE);
        else problem.setText(preferenceManager.getSubProblem());


        if (!preferenceManager.getPDF().equals("")) {
            pdfName.setText(preferenceManager.getPDF());
        }
    }

    public void getBitmapFromURL(OnCalLBack onCall, String strURL) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(strURL)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                onCall.onFailure(e);
            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) {
                assert response.body() != null;
                InputStream inputStream = response.body().byteStream();
                onCall.onSuccess(BitmapFactory.decodeStream(inputStream));
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        if (v.getId() == R.id.pic1) {
            if (bitmap1 != null)
                bitmap1.compress(Bitmap.CompressFormat.JPEG, 50, bs);
        }
        if (v.getId() == R.id.pic2) {
            if (bitmap2 != null)
                bitmap2.compress(Bitmap.CompressFormat.JPEG, 50, bs);
        }
        if (v.getId() == R.id.pic3) {
            if (bitmap3 != null)
                bitmap3.compress(Bitmap.CompressFormat.JPEG, 50, bs);
        }
        intent.putExtra("byteArray", bs.toByteArray());
        startActivity(intent);
    }

    interface OnCalLBack {
        void onSuccess(Bitmap bitmap);

        void onFailure(Throwable t);
    }
}