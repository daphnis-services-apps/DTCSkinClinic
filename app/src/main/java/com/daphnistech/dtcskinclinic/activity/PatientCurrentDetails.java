package com.daphnistech.dtcskinclinic.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.Signature;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.florent37.inlineactivityresult.InlineActivityResult;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class PatientCurrentDetails extends AppCompatActivity implements View.OnClickListener {
    public RelativeLayout pic1Layout, pic2Layout, pic3Layout;
    public ImageView uploadCardView;
    public ImageView pic1, pic2, pic3, image, back;
    public ImageView pic1Cancel, pic2Cancel, pic3Cancel, pdfCancel;
    public String diseaseName;
    public TextView pdfName;
    public boolean isPdfChoose = false;
    public LinearLayout imageLayout;
    ArrayAdapter<String> accountAdapter, numberAdapter, weekAdapter, problemAdapter;
    private Bitmap bitmap1, bitmap2, bitmap3;
    private ProgressBar pic1ProgressBar, pic2ProgressBar, pic3ProgressBar;
    private TextView heading, heading1;
    private EditText oldAge, comments;
    private TextView next, delete;
    private TextView affectedArea;
    private TextView viewArea;
    private String[] spinnerArray, numberArray, weekArray, problemArray;
    private CardView pdfUpload;
    private Spinner spinner, numberSpinner, weekSpinner, problemSpinner;
    private PreferenceManager preferenceManager;
    private View headerView;
    private RelativeLayout problemsLayout, imageView;

    private LinearLayout mContent;
    private Signature mSignature;
    private Bitmap bitmap;
    private TextView mClear, mGetSign, mCancel;
    private Dialog dialog;
    private View view;
    private boolean isLoaded;
    private String stringPDF;

    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_current_details);
        initViews();

        back.setOnClickListener(v -> finish());

        accountAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerArray);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(accountAdapter);

        numberAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, numberArray);
        numberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numberSpinner.setAdapter(numberAdapter);

        weekAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, weekArray);
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekSpinner.setAdapter(weekAdapter);

        if (!diseaseName.equals(Constant.SEX_DISEASE)) {
            problemAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, problemArray);
            problemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            problemSpinner.setAdapter(problemAdapter);
        }

        settingValues();

        comments.setOnTouchListener((v, event) -> {
            if (comments.hasFocus()) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                }
            }
            return false;
        });

        uploadCardView.setOnClickListener(v -> {
            isPdfChoose = false;
            ImagePicker.Companion.with(PatientCurrentDetails.this)
                    .crop()
                    .galleryOnly()
                    .cropSquare()
                    .compress(256)            //Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)//Final image resolution will be less than 1080 x 1080(Optional)
                    .start();
        });

        next.setOnClickListener(v -> {
            CustomProgressBar.showProgressBar(this, false);
            new Handler().postDelayed(() -> {
                savePath();
                preferenceManager.setOldAge(numberArray[numberSpinner.getSelectedItemPosition()] + " " + weekArray[weekSpinner.getSelectedItemPosition()]);
                preferenceManager.setDiseaseType(spinnerArray[spinner.getSelectedItemPosition()]);
                preferenceManager.setComments(comments.getText().toString());
                if (!diseaseName.equals(Constant.SEX_DISEASE))
                    preferenceManager.setSubProblem(problemArray[problemSpinner.getSelectedItemPosition()]);
                assert getSupportFragmentManager() != null;
                updateDisease(this);
            }, 500);
        });

        delete.setOnClickListener(v -> {
            deleteDisease();
        });

        pdfCancel.setOnClickListener(v -> {
            pdfName.setText("No File Selected");
            pdfCancel.setVisibility(View.GONE);
            preferenceManager.setPDF("");
        });

        pdfUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPdfChoose = true;
                new InlineActivityResult(PatientCurrentDetails.this)
                        .startForResult(new Intent().setAction(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/pdf"))
                        .onSuccess(result -> {
                            stringPDF = getStringPdf(result.getData().getData());
                            pdfName.setText("1 File Selected");
                            preferenceManager.setPDF(result.getData().getData().toString());
                            pdfCancel.setVisibility(View.VISIBLE);
                        })
                        .onFail(result -> Toast.makeText(PatientCurrentDetails.this, "Failed", Toast.LENGTH_SHORT).show());
            }
        });

        affectedArea.setOnClickListener(v -> show());

        viewArea.setOnClickListener(v -> {
            if (isLoaded) {
                if (dialog != null) {
                    dialog.show();
                } else {
                    show();
                }
            } else
                Toast.makeText(this, "Please Wait Image is being Loaded...", Toast.LENGTH_SHORT).show();
        });
    }

    public String getStringPdf(Uri filepath) {
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            inputStream = getContentResolver().openInputStream(filepath);

            byte[] buffer = new byte[1024];
            byteArrayOutputStream = new ByteArrayOutputStream();

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        byte[] pdfByteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(pdfByteArray, Base64.DEFAULT);
    }

    private void savePath() {
        String path = getApplicationContext().getExternalFilesDir("Me/" + diseaseName).getAbsolutePath();
        if (pic1.getDrawable() != null) {
            try {
                FileOutputStream mFileOutStream = new FileOutputStream(path + "/pic1" + ".PNG");
                ((BitmapDrawable) (pic1.getDrawable())).getBitmap().compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                preferenceManager.setPic1Path(path + "/pic1" + ".PNG");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            new File(path + "/pic1" + ".PNG").delete();
            preferenceManager.setPic1("");
            preferenceManager.setPic1Path("");
        }
        if (pic2.getDrawable() != null) {
            try {
                FileOutputStream mFileOutStream = new FileOutputStream(path + "/pic2" + ".PNG");
                ((BitmapDrawable) (pic2.getDrawable())).getBitmap().compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                preferenceManager.setPic2Path(path + "/pic2" + ".PNG");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            new File(path + "/pic2" + ".PNG").delete();
            preferenceManager.setPic2("");
            preferenceManager.setPic2Path("");
        }
        if (pic3.getDrawable() != null) {
            try {
                FileOutputStream mFileOutStream = new FileOutputStream(path + "/pic3" + ".PNG");
                ((BitmapDrawable) (pic3.getDrawable())).getBitmap().compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                preferenceManager.setPic3Path(path + "/pic3" + ".PNG");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            new File(path + "/pic3" + ".PNG").delete();
            preferenceManager.setPic3("");
            preferenceManager.setPic3Path("");
        }
        if (viewArea.getVisibility() == View.VISIBLE && new File(path + "/body" + ".PNG").exists())
            preferenceManager.setAffectedArea(path + "/body" + ".PNG");
        else {
            new File(path + "/body" + ".PNG").delete();
            preferenceManager.setAffectedArea("");
        }

        if (!preferenceManager.getPDF().equals("")) {
            try (FileOutputStream fos = new FileOutputStream(path + "/report.pdf")) {
                // To be short I use a corrupted PDF string, so make sure to use a valid one if you want to preview the PDF file
                byte[] decoder = Base64.decode(stringPDF, Base64.CRLF);

                fos.write(decoder);
                System.out.println("PDF File Saved");
                preferenceManager.setPDF(path + "/report.pdf");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else new File(path + "/report.pdf").delete();
    }

    private void updateDisease(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);
        MultipartBody.Part pic1;
        if (!preferenceManager.getPic1().equals("") && !preferenceManager.getPic1().equals("N/A")) {
            File file = new File(preferenceManager.getPic1Path());
            // Create a request body with file and image media type
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
            // Create MultipartBody.Part using file request-body,file name and part name
            pic1 = MultipartBody.Part.createFormData("file1", System.currentTimeMillis() + "_" + file.getName(), fileReqBody);
        } else {
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), "");
            pic1 = MultipartBody.Part.createFormData("file1", "", requestBody);
        }
        MultipartBody.Part pic2;
        if (!preferenceManager.getPic2().equals("") && !preferenceManager.getPic2().equals("N/A")) {
            File file = new File(preferenceManager.getPic2Path());
            // Create a request body with file and image media type
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
            // Create MultipartBody.Part using file request-body,file name and part name
            pic2 = MultipartBody.Part.createFormData("file2", System.currentTimeMillis() + "_" + file.getName(), fileReqBody);
        } else {
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), "");
            pic2 = MultipartBody.Part.createFormData("file2", "", requestBody);
        }
        MultipartBody.Part pic3;
        if (!preferenceManager.getPic3().equals("") && !preferenceManager.getPic3().equals("N/A")) {
            File file = new File(preferenceManager.getPic3Path());
            // Create a request body with file and image media type
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
            // Create MultipartBody.Part using file request-body,file name and part name
            pic3 = MultipartBody.Part.createFormData("file3", System.currentTimeMillis() + "_" + file.getName(), fileReqBody);
        } else {
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), "");
            pic3 = MultipartBody.Part.createFormData("file3", "", requestBody);
        }
        MultipartBody.Part pdf;
        if (!preferenceManager.getPDF().equals("") && !preferenceManager.getPDF().equals("N/A")) {
            //String path = Environment.getExternalStorageDirectory().getPath()+"/";
            // String newPath = preferenceManager.getPDF().replace("/document/primary:",path);
            //File file = new File(newPath);
            File file = new File(preferenceManager.getPDF());
            // Create a request body with file and image media type
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("application/pdf"), file);
            // Create MultipartBody.Part using file request-body,file name and part name
            pdf = MultipartBody.Part.createFormData("file4", System.currentTimeMillis() + "_" + file.getName(), fileReqBody);
        } else {
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), "");
            pdf = MultipartBody.Part.createFormData("file4", "", requestBody);
        }
        MultipartBody.Part affectedArea;
        if (!preferenceManager.getAffectedArea().equals("") && !preferenceManager.getAffectedArea().equals("N/A")) {
            File file = new File(preferenceManager.getAffectedArea());
            // Create a request body with file and image media type
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
            // Create MultipartBody.Part using file request-body,file name and part name
            affectedArea = MultipartBody.Part.createFormData("file5", System.currentTimeMillis() + "_" + file.getName(), fileReqBody);
        } else {
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), "");
            affectedArea = MultipartBody.Part.createFormData("file5", "", requestBody);
        }
        //Create request body with text description and text media type
        String oldAge = preferenceManager.getOldAge();
        String diseaseType = preferenceManager.getDiseaseType();
        String commentsString = preferenceManager.getComments().equals("") ? "N/A" : preferenceManager.getComments();
        String subProblem = preferenceManager.getSubProblem().equals("") ? "N/A" : preferenceManager.getSubProblem();
        RequestBody patient_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(new PreferenceManager(context, Constant.USER_DETAILS).getUserID()));
        RequestBody disease = RequestBody.create(MediaType.parse("text/plain"), diseaseName);
        RequestBody age = RequestBody.create(MediaType.parse("text/plain"), oldAge);
        RequestBody disease_type = RequestBody.create(MediaType.parse("text/plain"), diseaseType);
        RequestBody problem = RequestBody.create(MediaType.parse("text/plain"), subProblem);
        RequestBody comments = RequestBody.create(MediaType.parse("text/plain"), commentsString);
        Call<String> call = api.addCurrentDiseases(patient_id, disease, age, disease_type, problem, comments, pic1, pic2, pic3, pdf, affectedArea);
        //Call<String> call = api.addCurrentDiseases(String.valueOf(user_id), diseaseName, oldAge, diseaseType, commentsString, pic1, pic2, pic3, pic3);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Toast.makeText(context, diseaseName + " details Updated", Toast.LENGTH_SHORT).show();
                        CustomProgressBar.hideProgressBar();
                        finish();
                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(context, "Returned empty response", Toast.LENGTH_SHORT).show();
                        CustomProgressBar.hideProgressBar();
                    }
                } else if (response.errorBody() != null) {
                    Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                    CustomProgressBar.hideProgressBar();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                CustomProgressBar.hideProgressBar();
            }
        });
    }

    private void deleteDisease() {
        CustomProgressBar.showProgressBar(this, false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);
        //getting API Call
        Call<String> call = api.deleteDisease(
                new PreferenceManager(this, Constant.USER_DETAILS).getUserID(),
                diseaseName);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PatientCurrentDetails.this, diseaseName + " Deleted Successfully", Toast.LENGTH_LONG).show();
                    CustomProgressBar.hideProgressBar();
                    finish();
                } else if (response.errorBody() != null) {
                    Toast.makeText(PatientCurrentDetails.this, response.message(), Toast.LENGTH_SHORT).show();
                    CustomProgressBar.hideProgressBar();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(PatientCurrentDetails.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                CustomProgressBar.hideProgressBar();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void settingValues() {
        preferenceManager.setSteps(0);
        heading.setText(getIntent().getStringExtra("type"));
        if (!preferenceManager.getPic1().equals("N/A") && !preferenceManager.getPic1().equals("")) {
            preferenceManager.setSteps(1);
            pic1Layout.setVisibility(View.VISIBLE);
            imageLayout.setVisibility(View.VISIBLE);
            getBitmapFromURL(new OnCalLBack() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    runOnUiThread(() -> {
                        pic1.setImageBitmap(bitmap);
                        pic1ProgressBar.setVisibility(View.GONE);
                        pic1.setOnClickListener(PatientCurrentDetails.this);
                        String path = getApplicationContext().getExternalFilesDir("Me/" + getIntent().getStringExtra("type")).getAbsolutePath();
                        try {
                            FileOutputStream mFileOutStream = new FileOutputStream(path + "/pic1" + ".PNG");
                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
                    bitmap1 = bitmap;
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, preferenceManager.getPic1());
        } else pic1ProgressBar.setVisibility(View.GONE);
        if (!preferenceManager.getPic2().equals("N/A") && !preferenceManager.getPic2().equals("")) {
            preferenceManager.setSteps(2);
            pic2Layout.setVisibility(View.VISIBLE);
            imageLayout.setVisibility(View.VISIBLE);
            getBitmapFromURL(new OnCalLBack() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    runOnUiThread(() -> {
                        pic2.setImageBitmap(bitmap);
                        pic2ProgressBar.setVisibility(View.GONE);
                        pic2.setOnClickListener(PatientCurrentDetails.this);
                        String path = getApplicationContext().getExternalFilesDir("Me/" + getIntent().getStringExtra("type")).getAbsolutePath();
                        try {
                            FileOutputStream mFileOutStream = new FileOutputStream(path + "/pic2" + ".PNG");
                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
                    bitmap2 = bitmap;
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, preferenceManager.getPic2());
        } else pic2ProgressBar.setVisibility(View.GONE);
        if (!preferenceManager.getPic3().equals("N/A") && !preferenceManager.getPic3().equals("")) {
            preferenceManager.setSteps(3);
            pic3Layout.setVisibility(View.VISIBLE);
            imageLayout.setVisibility(View.VISIBLE);
            uploadCardView.setVisibility(View.GONE);
            getBitmapFromURL(new OnCalLBack() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    runOnUiThread(() -> {
                        pic3.setImageBitmap(bitmap);
                        pic3ProgressBar.setVisibility(View.GONE);
                        pic3.setOnClickListener(PatientCurrentDetails.this);
                        String path = getApplicationContext().getExternalFilesDir("Me/" + getIntent().getStringExtra("type")).getAbsolutePath();
                        try {
                            FileOutputStream mFileOutStream = new FileOutputStream(path + "/pic3" + ".PNG");
                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
                    bitmap3 = bitmap;
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, preferenceManager.getPic3());
        } else pic3ProgressBar.setVisibility(View.GONE);
        if (!preferenceManager.getAffectedArea().equals("N/A") && !preferenceManager.getAffectedArea().equals("")) {
            getBitmapFromURL(new OnCalLBack() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    runOnUiThread(() -> {
                        String path = getApplicationContext().getExternalFilesDir("Me/" + getIntent().getStringExtra("type")).getAbsolutePath();
                        try {
                            FileOutputStream mFileOutStream = new FileOutputStream(path + "/body" + ".PNG");
                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        isLoaded = true;
                    });
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, preferenceManager.getAffectedArea());
        }

        oldAge.setText(preferenceManager.getOldAge());
        //diseaseType.setText(preferenceManager.getDiseaseType());
        comments.setText(preferenceManager.getComments());
        CustomProgressBar.hideProgressBar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (diseaseName.equals(Constant.SKIN_DISEASE)) {
                heading1.setTextColor(getResources().getColor(R.color.loginChooser));
                window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
                headerView.setBackground(getDrawable(R.drawable.dashboard_header));
                next.setBackground(getDrawable(R.drawable.button_custom));
                imageView.setBackground(getDrawable(R.drawable.button_custom));
                image.setImageDrawable(getDrawable(R.drawable.skin_disease));
            } else if (diseaseName.equals(Constant.SEX_DISEASE)) {
                heading1.setTextColor(getResources().getColor(R.color.pink));
                window.setStatusBarColor(getResources().getColor(R.color.pink));
                headerView.setBackground(getDrawable(R.drawable.pink_header));
                next.setBackground(getDrawable(R.drawable.pink_button_custom));
                imageView.setBackground(getDrawable(R.drawable.pink_button_custom));
                image.setImageDrawable(getDrawable(R.drawable.sex_disease));
            } else if (diseaseName.equals(Constant.HAIR_PROBLEM)) {
                heading1.setTextColor(getResources().getColor(R.color.light_black));
                window.setStatusBarColor(getResources().getColor(R.color.light_black));
                headerView.setBackground(getDrawable(R.drawable.light_black_header));
                next.setBackground(getDrawable(R.drawable.light_black_button_custom));
                imageView.setBackground(getDrawable(R.drawable.light_black_button_custom));
                image.setImageDrawable(getDrawable(R.drawable.hair_problem));
            } else if (diseaseName.equals(Constant.COSMETOLOGY)) {
                heading1.setTextColor(getResources().getColor(R.color.palm));
                window.setStatusBarColor(getResources().getColor(R.color.palm));
                headerView.setBackground(getDrawable(R.drawable.palm_header));
                next.setBackground(getDrawable(R.drawable.palm_button_custom));
                imageView.setBackground(getDrawable(R.drawable.palm_button_custom));
                image.setImageDrawable(getDrawable(R.drawable.cosmetology));
            }
        }

        heading.setText(String.format("Add %s Info", diseaseName));
        heading1.setText(String.format("Add %s Info", diseaseName));
        oldAge.setText(preferenceManager.getOldAge());
        comments.setText(preferenceManager.getComments());

        spinner.setSelection(accountAdapter.getPosition(preferenceManager.getDiseaseType()));
        numberSpinner.setSelection(numberAdapter.getPosition(preferenceManager.getOldAge().split(" ")[0]));
        try {
            weekSpinner.setSelection(weekAdapter.getPosition(preferenceManager.getOldAge().split(" ")[1]));
        } catch (Exception e) {

        }
        if (!diseaseName.equals(Constant.SEX_DISEASE))
            problemSpinner.setSelection(problemAdapter.getPosition(preferenceManager.getSubProblem()));

        if (!preferenceManager.getAffectedArea().equals("N/A") && !preferenceManager.getAffectedArea().equals("")) {
            affectedArea.setVisibility(View.GONE);
            viewArea.setVisibility(View.VISIBLE);
        }

        if (!preferenceManager.getPDF().equals("") && !preferenceManager.getPDF().equals("N/A")) {
            pdfName.setText("1 file Selected");
            pdfCancel.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        back = findViewById(R.id.back);
        pic1Layout = findViewById(R.id.pic1Layout);
        pic1ProgressBar = findViewById(R.id.pic1ProgressBar);
        pic2Layout = findViewById(R.id.pic2Layout);
        pic2ProgressBar = findViewById(R.id.pic2ProgressBar);
        pic3Layout = findViewById(R.id.pic3Layout);
        pic3ProgressBar = findViewById(R.id.pic3ProgressBar);
        uploadCardView = findViewById(R.id.imageUpload);
        pic1 = findViewById(R.id.pic1);
        pic1Cancel = findViewById(R.id.pic1Cancel);
        pic2 = findViewById(R.id.pic2);
        pic1Cancel = findViewById(R.id.pic2Cancel);
        pic3 = findViewById(R.id.pic3);
        pic1Cancel = findViewById(R.id.pic3Cancel);
        image = findViewById(R.id.image);
        next = findViewById(R.id.submit);
        delete = findViewById(R.id.delete);
        imageView = findViewById(R.id.imageView);
        pdfUpload = findViewById(R.id.pdfUpload);
        pdfCancel = findViewById(R.id.pdfCancel);
        pdfName = findViewById(R.id.pdfName);
        spinner = findViewById(R.id.spinner);
        numberSpinner = findViewById(R.id.numberSpinner);
        weekSpinner = findViewById(R.id.weekSpinner);
        problemSpinner = findViewById(R.id.problemSpinner);
        heading = findViewById(R.id.text);
        heading1 = findViewById(R.id.text1);
        oldAge = findViewById(R.id.oldAge);
        comments = findViewById(R.id.diseaseProblem);
        headerView = findViewById(R.id.headerView);
        problemsLayout = findViewById(R.id.problemLayout);
        imageLayout = findViewById(R.id.imageLayout);
        affectedArea = findViewById(R.id.affectedArea);
        viewArea = findViewById(R.id.viewArea);
        diseaseName = getIntent().getStringExtra("type");
        preferenceManager = new PreferenceManager(this, diseaseName);
        setSpinnerArray();
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

    private void setSpinnerArray() {
        switch (diseaseName) {
            case Constant.SKIN_DISEASE:
                spinnerArray = getResources().getStringArray(R.array.skinDiseasesArray);
                problemArray = getResources().getStringArray(R.array.skinProblemsArray);
                break;
            case Constant.COSMETOLOGY:
                spinnerArray = getResources().getStringArray(R.array.cosmeticsArray);
                problemArray = getResources().getStringArray(R.array.cosmeticProblemsArray);
                break;
            case Constant.HAIR_PROBLEM:
                spinnerArray = getResources().getStringArray(R.array.hairProblemArray);
                problemArray = getResources().getStringArray(R.array.skinProblemsArray);
                break;
            case Constant.SEX_DISEASE:
                spinnerArray = getResources().getStringArray(R.array.sexProblemArray);
                problemsLayout.setVisibility(View.GONE);
                break;
        }
        numberArray = new String[31];
        for (int i = 0; i < 31; i++) {
            numberArray[i] = i + 1 + "";
        }
        weekArray = getResources().getStringArray(R.array.weekArray);
    }

    public void picCancel(View v) {
        if (v.getId() == R.id.pic1Cancel) {
            if (pic2Layout.getVisibility() == View.VISIBLE && pic3Layout.getVisibility() == View.VISIBLE) {
                pic1.setImageDrawable(pic2.getDrawable());
                pic2.setImageDrawable(pic3.getDrawable());
                pic3Layout.setVisibility(View.GONE);
                pic3.setImageDrawable(null);
            } else if (pic2Layout.getVisibility() == View.VISIBLE) {
                pic1.setImageDrawable(pic2.getDrawable());
                pic2Layout.setVisibility(View.GONE);
                pic2.setImageDrawable(null);
            } else if (pic3Layout.getVisibility() == View.VISIBLE) {
                pic1.setImageDrawable(pic3.getDrawable());
                pic3Layout.setVisibility(View.GONE);
                pic3.setImageDrawable(null);
            } else {
                pic1Layout.setVisibility(View.GONE);
                imageLayout.setVisibility(View.GONE);
                pic1.setImageDrawable(null);
            }
        } else if (v.getId() == R.id.pic2Cancel) {
            if (pic3Layout.getVisibility() == View.VISIBLE) {
                pic2.setImageDrawable(pic3.getDrawable());
                pic3Layout.setVisibility(View.GONE);
                pic3.setImageDrawable(null);
            } else {
                pic2Layout.setVisibility(View.GONE);
                pic2.setImageDrawable(null);
            }

        } else if (v.getId() == R.id.pic3Cancel) {
            pic3Layout.setVisibility(View.GONE);
            pic3.setImageDrawable(null);
        }
        preferenceManager.setSteps(preferenceManager.getSteps() - 1);
        uploadCardView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            if (preferenceManager.getSteps() == 3) {
                preferenceManager.setSteps(0);
            }
            if (preferenceManager.getSteps() == 0) {
                pic1.setImageURI(fileUri);
                pic1Layout.setVisibility(View.VISIBLE);
                imageLayout.setVisibility(View.VISIBLE);
                preferenceManager.setPic1(fileUri.toString());
                preferenceManager.setPic1Path(data.getData().getPath());
                preferenceManager.setSteps(1);
            } else if (preferenceManager.getSteps() == 1) {
                pic1.setImageURI(Uri.parse(preferenceManager.getPic1()));
                imageLayout.setVisibility(View.VISIBLE);
                pic1Layout.setVisibility(View.VISIBLE);
                pic2.setImageURI(fileUri);
                pic2Layout.setVisibility(View.VISIBLE);
                preferenceManager.setPic2(fileUri.toString());
                preferenceManager.setPic2Path(data.getData().getPath());
                preferenceManager.setSteps(2);
            } else if (preferenceManager.getSteps() == 2) {
                pic1.setImageURI(Uri.parse(preferenceManager.getPic1()));
                imageLayout.setVisibility(View.VISIBLE);
                pic1Layout.setVisibility(View.VISIBLE);
                pic2.setImageURI(Uri.parse(preferenceManager.getPic2()));
                pic2Layout.setVisibility(View.VISIBLE);
                pic3.setImageURI(fileUri);
                pic3Layout.setVisibility(View.VISIBLE);
                uploadCardView.setVisibility(View.GONE);
                preferenceManager.setPic3(fileUri.toString());
                preferenceManager.setPic3Path(data.getData().getPath());
                preferenceManager.setSteps(3);
            }
        }
    }

    private void show() {
        // Dialog Function
        dialog = new Dialog(this);
        // Removing the features of Normal Dialogs
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_signature);
        dialog.setCancelable(true);

        dialog_action();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void dialog_action() {
        mContent = dialog.findViewById(R.id.linearLayout);
        mSignature = new Signature(this);
        if (preferenceManager.getAffectedArea().equals("") || preferenceManager.getAffectedArea().equals("N/A"))
            mSignature.setBackground(getResources().getDrawable(R.drawable.body));
        else
            mSignature.setBackground(Drawable.createFromPath(getApplicationContext().getExternalFilesDir("Me/" + diseaseName).getAbsolutePath() + "/body" + ".PNG"));
        // Dynamically generating Layout through java code
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mClear = dialog.findViewById(R.id.clear);
        mGetSign = dialog.findViewById(R.id.getsign);
        mGetSign.setEnabled(true);
        mCancel = dialog.findViewById(R.id.cancel);
        view = mContent;

        mClear.setOnClickListener(v -> {
            Log.v("log_tag", "Panel Cleared");
            if (!preferenceManager.getAffectedArea().equals("") || !preferenceManager.getAffectedArea().equals("N/A")) {
                dialog.dismiss();
                preferenceManager.setAffectedArea("");
                show();
            }
            mSignature.clear();
            viewArea.setVisibility(View.GONE);
            affectedArea.setVisibility(View.VISIBLE);
        });

        mGetSign.setOnClickListener(v -> {

            Log.v("log_tag", "Panel Saved");
            view.setDrawingCacheEnabled(true);
            save(view);
            dialog.dismiss();
            viewArea.setVisibility(View.VISIBLE);
            affectedArea.setVisibility(View.GONE);

        });
        mCancel.setOnClickListener(v -> {
            Log.v("log_tag", "Panel Canceled");
            mSignature.undo();
        });
        dialog.show();
    }

    public void save(View v) {
        String path = getApplicationContext().getExternalFilesDir("Me/" + diseaseName).getAbsolutePath();
        bitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        try {
            // Output the file
            OutputStream mFileOutStream = new FileOutputStream(path + "/body" + ".PNG");
            Log.v("log_tag", "path" + mFileOutStream);
            // Convert the output file to Image such as .png
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
            mFileOutStream.flush();
            mFileOutStream.close();

        } catch (Exception e) {
            Log.v("log_tag", e.toString());
        }
    }

    interface OnCalLBack {
        void onSuccess(Bitmap bitmap);

        void onFailure(Throwable t);
    }
}