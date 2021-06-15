package com.daphnistech.dtcskinclinic.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
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

public class ViewPatientDetails extends AppCompatActivity implements View.OnClickListener {
    TextView name, age, address, gender;
    EditText editName, editAge, editAddress, editPin;
    ImageView skinDisease, cosmetology, hairProblem, sexDisease;
    ImageView editSkinDisease, editCosmetology, editHairProblem, editSexDisease;
    ImageView skinDiseaseContinue, cosmetologyContinue, hairProblemContinue, sexDiseaseContinue;
    ConstraintLayout skinDiseaseLayout, cosmetologyLayout, hairProblemLayout, sexDiseaseLayout;
    ConstraintLayout editSkinDiseaseLayout, editCosmetologyLayout, editHairProblemLayout, editSexDiseaseLayout;
    RadioButton male, female, others;
    CircleImageView profilePic, editProfilePic;
    ArrayAdapter<String> accountAdapter;
    private Context context;
    private TextView editProfile, updateProfile;
    private CardView viewPanel, editPanel;
    private PreferenceManager preferenceManager;
    private String imagePath = null;
    private String[] stateArray;
    private Spinner spinner;
    private Uri fileUri;
    private ImageView back;

    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient_details);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }
        initViews();

        back.setOnClickListener(v -> finish());

        editProfile.setOnClickListener(v -> {
            viewPanel.setVisibility(View.GONE);
            editProfile.setVisibility(View.GONE);
            editPanel.setVisibility(View.VISIBLE);
        });

        editAddress.setOnTouchListener((v, event) -> {
            if (editAddress.hasFocus()) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                }
            }
            return false;
        });

        updateProfile.setOnClickListener(v -> {
            if (validateFields()) {
                updateProfile();
            } else {
                Toast.makeText(context, "Please fill all details", Toast.LENGTH_SHORT).show();
            }
        });

        editProfilePic.setOnClickListener(v -> ImagePicker.Companion.with(ViewPatientDetails.this)
                .crop()
                .galleryOnly()
                .compress(256)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)//Final image resolution will be less than 1080 x 1080(Optional)
                .start());
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
        RequestBody patient_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(preferenceManager.getUserID()));
        RequestBody mobile = RequestBody.create(MediaType.parse("text/plain"), preferenceManager.getMobile());
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), editName.getText().toString());
        RequestBody age = RequestBody.create(MediaType.parse("text/plain"), editAge.getText().toString());
        RequestBody gender = RequestBody.create(MediaType.parse("text/plain"), male.isChecked() ? Constant.MALE : female.isChecked() ? Constant.FEMALE : Constant.OTHERS);
        RequestBody address = RequestBody.create(MediaType.parse("text/plain"), editAddress.getText().toString().isEmpty() ? "N/A" : editAddress.getText().toString());
        RequestBody state = RequestBody.create(MediaType.parse("text/plain"), stateArray[spinner.getSelectedItemPosition()]);
        RequestBody pin = RequestBody.create(MediaType.parse("text/plain"), editPin.getText().toString().isEmpty() ? "N/A" : editPin.getText().toString());
        Call<String> call = api.updatePatient(patient_id, mobile, name, age, gender, address, state, pin, image);

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
                                Toast.makeText(context, "Patients Details Updated", Toast.LENGTH_SHORT).show();
                                getPatientDetails();
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

    private boolean validateFields() {
        if (editName.getText().toString().isEmpty())
            return false;

        return !editAge.getText().toString().isEmpty();
    }

    private void getPatientDetails() {
        CustomProgressBar.showProgressBar(context, false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call = api.getPatientDetails(getIntent().getIntExtra("id", 0));

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (!jsonObject.getBoolean("error")) {
                                JSONObject patientDetails = jsonObject.getJSONObject("patientDetails");
                                JSONArray currentDetails = jsonObject.getJSONArray("currentDetails");
                                setPatientDetails(patientDetails, currentDetails);
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

    @SuppressLint({"UseCompatLoadingForDrawables"})
    @SuppressWarnings("deprecation")
    private void setPatientDetails(JSONObject patientDetails, JSONArray currentDetails) {
        try {
            Glide.with(this).load(patientDetails.getString("image")).placeholder(R.drawable.doctor_plus).into(profilePic);
            name.setText(patientDetails.getString("name"));
            preferenceManager.setName(name.getText().toString());
            preferenceManager.setProfileImage(patientDetails.getString("image"));
            editName.setText(patientDetails.getString("name"));
            age.setText(patientDetails.getString("age"));
            editAge.setText(patientDetails.getString("age"));
            gender.setText(patientDetails.getString("gender"));
            switch (patientDetails.getString("gender")) {
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
            address.setText(String.format("%s, %s, %s", patientDetails.getString("address"), patientDetails.getString("state"), patientDetails.getString("pin")));
            editAddress.setText(patientDetails.getString("address"));
            spinner.setSelection(accountAdapter.getPosition(patientDetails.getString("state")));
            editPin.setText(patientDetails.getString("pin"));

            for (int i = 0; i < currentDetails.length(); i++) {
                JSONObject currentDisease = currentDetails.getJSONObject(i);
                PreferenceManager preferenceManager = null;
                if (currentDisease.getString("disease").equals(Constant.SKIN_DISEASE)) {
                    skinDisease.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
                    editSkinDisease.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
                    preferenceManager = new PreferenceManager(context, Constant.SKIN_DISEASE);
                    skinDiseaseLayout.setOnClickListener(this);
                    skinDiseaseContinue.setVisibility(View.VISIBLE);
                }
                if (currentDisease.getString("disease").equals(Constant.COSMETOLOGY)) {
                    cosmetology.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
                    editCosmetology.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
                    preferenceManager = new PreferenceManager(context, Constant.COSMETOLOGY);
                    cosmetologyLayout.setOnClickListener(this);
                    cosmetologyContinue.setVisibility(View.VISIBLE);
                }
                if (currentDisease.getString("disease").equals(Constant.HAIR_PROBLEM)) {
                    hairProblem.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
                    editHairProblem.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
                    preferenceManager = new PreferenceManager(context, Constant.HAIR_PROBLEM);
                    hairProblemLayout.setOnClickListener(this);
                    hairProblemContinue.setVisibility(View.VISIBLE);
                }
                if (currentDisease.getString("disease").equals(Constant.SEX_DISEASE)) {
                    sexDisease.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
                    editSexDisease.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
                    preferenceManager = new PreferenceManager(context, Constant.SEX_DISEASE);
                    sexDiseaseLayout.setOnClickListener(this);
                    sexDiseaseContinue.setVisibility(View.VISIBLE);
                }
                assert preferenceManager != null;
                preferenceManager.setPic1Path("");
                preferenceManager.setPic2Path("");
                preferenceManager.setPic3Path("");
                preferenceManager.setOldAge(currentDisease.getString("old_age"));
                preferenceManager.setDiseaseType(currentDisease.getString("disease_type"));
                preferenceManager.setSubProblem(currentDisease.getString("sub_problem"));
                preferenceManager.setComments(currentDisease.getString("comments"));
                preferenceManager.setPic1(currentDisease.getString("first_photo"));
                preferenceManager.setPic2(currentDisease.getString("second_photo"));
                preferenceManager.setPic3(currentDisease.getString("third_photo"));
                preferenceManager.setAffectedArea(currentDisease.getString("affected_area"));
                preferenceManager.setPDF(currentDisease.getString("pdf"));
                editPanel.setVisibility(View.GONE);
                if (preferenceManager.getLoginType().equals(Constant.PATIENT))
                    editProfile.setVisibility(View.VISIBLE);
                else editProfile.setVisibility(View.GONE);
                viewPanel.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        context = ViewPatientDetails.this;
        back = findViewById(R.id.back);
        editProfile = findViewById(R.id.editProfile);
        updateProfile = findViewById(R.id.submit);
        viewPanel = findViewById(R.id.viewPanel);
        editPanel = findViewById(R.id.editPanel);
        editName = findViewById(R.id.editName);
        name = findViewById(R.id.name);
        age = findViewById(R.id.age);
        editAge = findViewById(R.id.editAge);
        gender = findViewById(R.id.gender);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        others = findViewById(R.id.others);
        address = findViewById(R.id.address);
        editAddress = findViewById(R.id.editAddress);
        editPin = findViewById(R.id.editPin);
        skinDisease = findViewById(R.id.skinDiseaseImage);
        editSkinDisease = findViewById(R.id.editSkinDiseaseImage);
        cosmetology = findViewById(R.id.cosmetologyImage);
        editCosmetology = findViewById(R.id.editCosmetologyImage);
        hairProblem = findViewById(R.id.hairProblemImage);
        editHairProblem = findViewById(R.id.editHairProblemImage);
        sexDisease = findViewById(R.id.sexProblemImage);
        editSexDisease = findViewById(R.id.editSexProblemImage);
        skinDiseaseLayout = findViewById(R.id.skinDisease);
        editSkinDiseaseLayout = findViewById(R.id.editSkinDisease);
        cosmetologyLayout = findViewById(R.id.cosmetology);
        editCosmetologyLayout = findViewById(R.id.editCosmetology);
        hairProblemLayout = findViewById(R.id.hairProblem);
        editHairProblemLayout = findViewById(R.id.editHairProblem);
        sexDiseaseLayout = findViewById(R.id.sexProblem);
        editSexDiseaseLayout = findViewById(R.id.editSexProblem);
        skinDiseaseContinue = findViewById(R.id.skinDiseaseContinue);
        cosmetologyContinue = findViewById(R.id.cosmetologyContinue);
        hairProblemContinue = findViewById(R.id.hairProblemContinue);
        sexDiseaseContinue = findViewById(R.id.sexProblemContinue);
        editProfilePic = findViewById(R.id.editProfilePic);
        profilePic = findViewById(R.id.profilePic);
        spinner = findViewById(R.id.spinner);
        stateArray = getResources().getStringArray(R.array.india_states);

        preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);

        accountAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, stateArray);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(accountAdapter);
        if (preferenceManager.getLoginType().equals(Constant.PATIENT))
            editProfile.setVisibility(View.VISIBLE);
        else editProfile.setVisibility(View.GONE);

        Glide.with(this).load(preferenceManager.getProfileImage()).placeholder(R.drawable.doctor_plus).into(editProfilePic);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(context, PatientEditCurrentDetails.class);
        if (v.getId() == R.id.skinDisease) {
            intent.putExtra("type", Constant.SKIN_DISEASE);
        }
        if (v.getId() == R.id.cosmetology) {
            intent.putExtra("type", Constant.COSMETOLOGY);
        }
        if (v.getId() == R.id.hairProblem) {
            intent.putExtra("type", Constant.HAIR_PROBLEM);
        }
        if (v.getId() == R.id.sexProblem) {
            intent.putExtra("type", Constant.SEX_DISEASE);
        }
        startActivity(intent);
    }

    public void editOptions(View v) {
        Intent intent = new Intent(context, PatientCurrentDetails.class);
        if (v.getId() == R.id.editSkinDisease) {
            intent.putExtra("type", Constant.SKIN_DISEASE);
        }
        if (v.getId() == R.id.editCosmetology) {
            intent.putExtra("type", Constant.COSMETOLOGY);
        }
        if (v.getId() == R.id.editHairProblem) {
            intent.putExtra("type", Constant.HAIR_PROBLEM);
        }
        if (v.getId() == R.id.editSexProblem) {
            intent.putExtra("type", Constant.SEX_DISEASE);
        }
        startActivity(intent);
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
        setDefaultChecks();
        getPatientDetails();
    }

    private void setDefaultChecks() {
        skinDisease.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_cancel__red_24));
        editSkinDisease.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_cancel__red_24));
        cosmetology.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_cancel__red_24));
        editCosmetology.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_cancel__red_24));
        hairProblem.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_cancel__red_24));
        editHairProblem.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_cancel__red_24));
        sexDisease.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_cancel__red_24));
        editSexDisease.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_cancel__red_24));
        skinDiseaseContinue.setVisibility(View.GONE);
        cosmetologyContinue.setVisibility(View.GONE);
        hairProblemContinue.setVisibility(View.GONE);
        sexDiseaseContinue.setVisibility(View.GONE);
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