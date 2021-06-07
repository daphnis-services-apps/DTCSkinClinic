package com.daphnistech.dtcskinclinic.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class PatientEditCurrentDetails extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    private Bitmap bitmap1, bitmap2, bitmap3, bitmap4;
    private ProgressBar pic1ProgressBar, pic2ProgressBar, pic3ProgressBar;
    private CardView pic1Layout, pic2Layout, pic3Layout, pdfLayout;
    private ImageView pic1, pic2, pic3, image, back;
    private String diseaseName;
    private TextView pdfName, viewArea;
    private TextView heading, heading1;
    private TextView oldAge, comments, diseaseType, problem;
    private PreferenceManager preferenceManager;
    private View headerView;
    private RelativeLayout problemsLayout, imageView;
    private LinearLayout imageLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_edit_current_details);
        initViews();
        back.setOnClickListener(v -> finish());
        CustomProgressBar.showProgressBar(context, false);
        new Handler().postDelayed(this::settingValues, 100);
    }

    private void initViews() {
        context = PatientEditCurrentDetails.this;
        back = findViewById(R.id.back);
        pic1Layout = findViewById(R.id.pic1Layout);
        pic1ProgressBar = findViewById(R.id.pic1ProgressBar);
        pic2Layout = findViewById(R.id.pic2Layout);
        pic2ProgressBar = findViewById(R.id.pic2ProgressBar);
        pic3Layout = findViewById(R.id.pic3Layout);
        pic3ProgressBar = findViewById(R.id.pic3ProgressBar);
        pdfLayout = findViewById(R.id.pdfLayout);
        pic1 = findViewById(R.id.pic1);
        pic2 = findViewById(R.id.pic2);
        pic3 = findViewById(R.id.pic3);
        image = findViewById(R.id.image);
        imageView = findViewById(R.id.imageView);
        viewArea = findViewById(R.id.viewArea);
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
        progressBar = findViewById(R.id.progressBar);
        diseaseName = getIntent().getStringExtra("type");
        preferenceManager = new PreferenceManager(this, diseaseName);
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
                        pic1.setOnClickListener(PatientEditCurrentDetails.this);
                    });
                    bitmap1 = bitmap;
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, preferenceManager.getPic1());
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
                        pic2.setOnClickListener(PatientEditCurrentDetails.this);
                    });
                    bitmap2 = bitmap;
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, preferenceManager.getPic2());
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
                        pic3.setOnClickListener(PatientEditCurrentDetails.this);
                    });
                    bitmap3 = bitmap;

                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, preferenceManager.getPic3());
        }

        if (!preferenceManager.getAffectedArea().equals("N/A") && !preferenceManager.getAffectedArea().equals("")) {
            getBitmapFromURL(new OnCalLBack() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        viewArea.setVisibility(View.VISIBLE);
                        viewArea.setOnClickListener(PatientEditCurrentDetails.this);
                    });
                    bitmap4 = bitmap;

                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, preferenceManager.getAffectedArea());
        } else {
            progressBar.setVisibility(View.GONE);
            viewArea.setVisibility(View.VISIBLE);
            viewArea.setText("Patient doesn't select Affected Area");
        }

        if (!preferenceManager.getPDF().equals("N/A") && !preferenceManager.getPDF().equals("")) {
            pdfName.setText("Downloading PDF");
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(preferenceManager.getPDF())
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {

                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                    int totalBytes = (int) response.body().contentLength();
                    InputStream inputStream = response.body().byteStream();
                    try {
                        String path1 = getApplicationContext().getExternalFilesDir("Me/" + diseaseName).getAbsolutePath();
                        FileOutputStream fileOutputStream = new FileOutputStream(path1 + "/report.pdf");
                        int loaded = 0;
                        int read;
                        byte[] bytes = new byte[8129];
                        while ((read = inputStream.read(bytes)) != -1) {
                            fileOutputStream.write(bytes, 0, read);
                            loaded += read;
                            float percent = ((100 * loaded) / totalBytes);
                            runOnUiThread(() -> pdfName.setText("Downloading PDF " + percent + "%"));
                        }
                        runOnUiThread(() -> {
                            pdfName.setText("View PDF Report");
                            pdfLayout.setOnClickListener(v -> openPDF());
                        });
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        Toast.makeText(context, e1.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
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


        if (preferenceManager.getPDF().equals("") || preferenceManager.getPDF().equals("N/A")) {
            pdfName.setText("No PDF Report Attached");
        } else {
            pdfName.setText("Downloading PDF 0.0 %");
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
        if (v.getId() == R.id.viewArea) {
            if (bitmap4 != null)
                bitmap4.compress(Bitmap.CompressFormat.JPEG, 50, bs);
        }
        intent.putExtra("byteArray", bs.toByteArray());
        startActivity(intent);
    }

    private void openPDF() {
        String path = getApplicationContext().getExternalFilesDir("Me/" + diseaseName).getAbsolutePath();
        File file = new File(path + "/report.pdf");
        Intent target = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        target.setDataAndType(uri, "application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Instruct the user to install a PDF reader here, or something
        }
    }

    interface OnCalLBack {
        void onSuccess(Bitmap bitmap);

        void onFailure(Throwable t);
    }
}