package com.daphnistech.dtcskinclinic.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ViewDoctorDetails extends AppCompatActivity {
    TextView name, age, designation, gender, consultationFees;
    EditText editName, editAge, editDesignation, editConsultation;
    RadioButton male, female, others;
    CircleImageView profilePic, editProfilePic;
    private Context context;
    private TextView editProfile, updateProfile;
    private CardView viewPanel, editPanel;
    private PreferenceManager preferenceManager;
    private String imagePath = null;
    private Uri fileUri;
    private ImageView back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_doctor_details);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }
        initViews();

        editProfile.setOnClickListener(v -> {
            viewPanel.setVisibility(View.GONE);
            editProfile.setVisibility(View.GONE);
            editPanel.setVisibility(View.VISIBLE);
        });

        updateProfile.setOnClickListener(v -> {
            if (validateFields()) {
                updateProfile();
            } else {
                Toast.makeText(context, "Please fill all details", Toast.LENGTH_SHORT).show();
            }
        });

        editProfilePic.setOnClickListener(v -> ImagePicker.Companion.with(ViewDoctorDetails.this)
                .crop()
                .galleryOnly()
                .compress(256)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        );

        back.setOnClickListener(v -> finish());
    }

    private boolean validateFields() {
        if (editName.getText().toString().isEmpty())
            return false;

        return !editAge.getText().toString().isEmpty();
    }

    private void initViews() {
        context = ViewDoctorDetails.this;
        editProfile = findViewById(R.id.editProfile);
        updateProfile = findViewById(R.id.submit);
        viewPanel = findViewById(R.id.viewPanel);
        editPanel = findViewById(R.id.editPanel);
        editName = findViewById(R.id.editName);
        name = findViewById(R.id.name);
        age = findViewById(R.id.age);
        editAge = findViewById(R.id.editAge);
        gender = findViewById(R.id.gender);
        back = findViewById(R.id.back);
        designation = findViewById(R.id.designation);
        consultationFees = findViewById(R.id.consultationFees);
        editDesignation = findViewById(R.id.editDesignation);
        editConsultation = findViewById(R.id.editConsultationFees);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        others = findViewById(R.id.others);
        editProfilePic = findViewById(R.id.editProfilePic);
        profilePic = findViewById(R.id.profilePic);

        preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);

        if (preferenceManager.getLoginType().equals(Constant.DOCTOR))
            editProfile.setVisibility(View.VISIBLE);

        Glide.with(this).load(preferenceManager.getProfileImage()).placeholder(R.drawable.doctor_plus).into(editProfilePic);
    }

    private void updateProfile() {
        CustomProgressBar.showProgressBar(context, false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        //getting API Call
        MultipartBody.Part image;
        if (imagePath != null) {
            File file = new File(imagePath);
            // Create a request body with file and image media type
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
            // Create MultipartBody.Part using file request-body,file name and part name
            image = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);
        } else {
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), "");
            image = MultipartBody.Part.createFormData("image", "", requestBody);
        }
        //Create request body with text description and text media type
        RequestBody doctor_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(preferenceManager.getUserID()));
        RequestBody mobile = RequestBody.create(MediaType.parse("text/plain"), preferenceManager.getMobile());
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), editName.getText().toString());
        RequestBody age = RequestBody.create(MediaType.parse("text/plain"), editAge.getText().toString());
        RequestBody gender = RequestBody.create(MediaType.parse("text/plain"), male.isChecked() ? Constant.MALE : female.isChecked() ? Constant.FEMALE : Constant.OTHERS);
        RequestBody consultationFees = RequestBody.create(MediaType.parse("text/plain"), editConsultation.getText().toString().isEmpty() ? "N/A" : editConsultation.getText().toString());
        RequestBody designation = RequestBody.create(MediaType.parse("text/plain"), editDesignation.getText().toString().isEmpty() ? "N/A" : editDesignation.getText().toString());
        Call<String> call = api.updateDoctor(doctor_id, mobile, name, age, gender, designation, consultationFees, image);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    //getting Response Body
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (!jsonObject.getBoolean("error")) {
                                CustomProgressBar.hideProgressBar();
                                Toast.makeText(context, "Doctors Details Updated", Toast.LENGTH_SHORT).show();
                                getDoctorDetails();
                            } else {
                                Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                CustomProgressBar.hideProgressBar();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            CustomProgressBar.hideProgressBar();
                        }

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(context, "Returned empty response", Toast.LENGTH_SHORT).show();
                        CustomProgressBar.hideProgressBar();
                    }
                } else if (response.errorBody() != null) {
                    //On Error Body Response(email error)
                    Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                    CustomProgressBar.hideProgressBar();
                }

            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                CustomProgressBar.hideProgressBar();
            }
        });
    }

    private void getDoctorDetails() {
        CustomProgressBar.showProgressBar(context, false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call = api.getDoctorDetails(getIntent().getIntExtra("id", 0));

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (!jsonObject.getBoolean("error")) {
                                JSONObject patientDetails = jsonObject.getJSONObject("doctorDetails");
                                setDoctorDetails(patientDetails);
                            } else {
                                Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        //Parsing the JSON

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(context, "Returned empty response", Toast.LENGTH_SHORT).show();
                    }
                } else if (response.errorBody() != null) {
                    Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                }
                CustomProgressBar.hideProgressBar();
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                CustomProgressBar.hideProgressBar();
            }
        });
    }

    private void setDoctorDetails(JSONObject doctorDetails) {
        try {
            Glide.with(this).load(doctorDetails.getString("image")).placeholder(R.drawable.doctor_plus).into(profilePic);
            name.setText(doctorDetails.getString("name"));
            preferenceManager.setName(name.getText().toString());
            preferenceManager.setProfileImage(doctorDetails.getString("image"));
            editName.setText(doctorDetails.getString("name"));
            age.setText(doctorDetails.getString("age"));
            editAge.setText(doctorDetails.getString("age"));
            gender.setText(doctorDetails.getString("gender"));
            switch (doctorDetails.getString("gender")) {
                case Constant.MALE:
                    male.setChecked(true);
                    break;
                case Constant.FEMALE:
                    female.setChecked(true);
                    break;
                case Constant.OTHERS:
                    others.setChecked(true);
                    break;
            }
            designation.setText(doctorDetails.getString("designation"));
            editDesignation.setText(doctorDetails.getString("designation"));
            consultationFees.setText(doctorDetails.getString("consultation_fees"));
            editConsultation.setText(doctorDetails.getString("consultation_fees"));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (editPanel.getVisibility() == View.VISIBLE) {
            editPanel.setVisibility(View.GONE);
            editProfile.setVisibility(View.VISIBLE);
            viewPanel.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDoctorDetails();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            fileUri = data.getData();
            imagePath = fileUri.getPath();
            editProfilePic.setImageURI(fileUri);
        }
    }
}