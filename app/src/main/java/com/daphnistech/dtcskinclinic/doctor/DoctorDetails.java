package com.daphnistech.dtcskinclinic.doctor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.activity.DoctorDashboard;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

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

public class DoctorDetails extends Fragment {
    public CircleImageView profilePic;
    EditText name, age, designation, consultationFees;
    TextView next;
    RadioButton male, female, others;
    ImageView back;
    PreferenceManager preferenceManager;
    String token;

    public DoctorDetails() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doctor_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        settingValues();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(getActivity(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get new FCM registration token
                token = task.getResult();
            }
        });

        back.setOnClickListener(v -> getActivity().finish());

        next.setOnClickListener(v -> {
            if (validateFields()) {
                setValues();
                sendToDashboard();
                //getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new AddCertificates()).addToBackStack("user").commit();
            } else {
                Toast.makeText(getActivity(), "Fill all required fields", Toast.LENGTH_SHORT).show();
            }
        });

        profilePic.setOnClickListener(v -> ImagePicker.Companion.with(getActivity())
                .crop()
                .galleryOnly()
                .compress(256)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)//Final image resolution will be less than 1080 x 1080(Optional)
                .start());

    }

    private void setValues() {
        preferenceManager.setName(name.getText().toString());
        preferenceManager.setAge(age.getText().toString());
        preferenceManager.setGender(male.isChecked() ? Constant.MALE : female.isChecked() ? Constant.FEMALE : Constant.OTHERS);
        preferenceManager.setDesignation(designation.getText().toString());
        preferenceManager.setConsultationFees(consultationFees.getText().toString());
    }

    private void sendToDashboard() {
        CustomProgressBar.showProgressBar(getActivity(), false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        MultipartBody.Part image;
        if (!preferenceManager.getProfileImage().equals("")) {
            File file = new File(preferenceManager.getProfileImage());
            // Create a request body with file and image media type
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
            // Create MultipartBody.Part using file request-body,file name and part name
            image = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);
        } else {
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), "");
            image = MultipartBody.Part.createFormData("image", "", requestBody);
        }
        //Create request body with text description and text media type
        RequestBody mobile = RequestBody.create(MediaType.parse("text/plain"), preferenceManager.getMobile());
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), this.name.getText().toString());
        RequestBody age = RequestBody.create(MediaType.parse("text/plain"), this.age.getText().toString());
        RequestBody gender = RequestBody.create(MediaType.parse("text/plain"), male.isChecked() ? Constant.MALE : female.isChecked() ? Constant.FEMALE : Constant.OTHERS);
        RequestBody designation = RequestBody.create(MediaType.parse("text/plain"), this.designation.getText().toString());
        RequestBody consultationFees = RequestBody.create(MediaType.parse("text/plain"), this.consultationFees.getText().toString());
        RequestBody isOnline = RequestBody.create(MediaType.parse("text/plain"), preferenceManager.isOnline());
        RequestBody fcmToken = RequestBody.create(MediaType.parse("text/plain"), token);
        @SuppressLint("HardwareIds") RequestBody deviceToken = RequestBody.create(MediaType.parse("text/plain"), Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID));

        //getting API Call
        @SuppressLint("HardwareIds") Call<String> call = api.loginDoctor(name, mobile, age, gender, designation, consultationFees, isOnline, fcmToken, deviceToken, image);

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
                                setPreferences(jsonObject.getJSONObject("user"));
                            } else {
                                Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Parsing the JSON

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");

                    }
                } else if (response.errorBody() != null) {
                    //On Error Body Response(email error)

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getActivity(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateFields() {
        if (name.getText().toString().isEmpty()) {
            return false;
        }
        if (age.getText().toString().isEmpty()) {
            return false;
        }
        if (designation.getText().toString().isEmpty()) {
            return false;
        }
        return !consultationFees.getText().toString().isEmpty();
    }

    private void setPreferences(JSONObject user) throws JSONException {
        preferenceManager.setUserID(user.getInt("doctor_id"));
        preferenceManager.setName(user.getString("name"));
        preferenceManager.setAge(user.getString("age"));
        preferenceManager.setProfileImage(user.getString("image"));
        preferenceManager.setGender(user.getString("gender"));
        preferenceManager.setDesignation(user.getString("designation"));
        preferenceManager.setConsultationFees(user.getString("consultation_fees"));
        preferenceManager.setLoggedIn(true);
        CustomProgressBar.hideProgressBar();
        getActivity().startActivity(new Intent(getActivity(), DoctorDashboard.class));
        getActivity().finish();
    }

    private void initViews(View view) {
        name = view.findViewById(R.id.name);
        age = view.findViewById(R.id.age);
        designation = view.findViewById(R.id.designation);
        consultationFees = view.findViewById(R.id.consultationFees);
        next = view.findViewById(R.id.submit);
        back = view.findViewById(R.id.back);
        male = view.findViewById(R.id.male);
        female = view.findViewById(R.id.female);
        others = view.findViewById(R.id.others);
        profilePic = view.findViewById(R.id.profilePic);
    }

    private void settingValues() {
        preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);
        name.setText(preferenceManager.getName());
        age.setText(preferenceManager.getAge());
        if (preferenceManager.getProfileImage().isEmpty())
            profilePic.setImageResource(R.drawable.doctor_plus);
        else profilePic.setImageURI(Uri.parse(preferenceManager.getProfileImage()));
        switch (preferenceManager.getGender()) {
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

        designation.setText(preferenceManager.getDesignation());
        consultationFees.setText(preferenceManager.getConsultationFees());
    }
}