package com.daphnistech.dtcskinclinic.doctor;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class DoctorDetails extends Fragment {
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
                sendToDashboard();
            } else {
                Toast.makeText(getActivity(), "Fill all required fields", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendToDashboard() {
        CustomProgressBar.showProgressBar(getActivity(),false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        //getting API Call
        @SuppressLint("HardwareIds") Call<String> call = api.loginDoctor(name.getText().toString(), preferenceManager.getMobile(), age.getText().toString(), male.isChecked() ? Constant.MALE : female.isChecked() ? Constant.FEMALE : Constant.OTHERS, designation.getText().toString(), consultationFees.getText().toString(), token, Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID));

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
        preferenceManager.setName(user.getString("name"));
        preferenceManager.setAge(user.getString("age"));
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
    }

    private void settingValues() {
        preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);
        name.setText(preferenceManager.getName());
        age.setText(preferenceManager.getAge());

        switch (preferenceManager.getGender()) {
            case "male":
                male.setChecked(true);
                break;
            case "female":
                female.setChecked(true);
                break;
            case "others":
                others.setChecked(true);
                break;
        }

        designation.setText(preferenceManager.getDesignation());
        consultationFees.setText(preferenceManager.getConsultationFees());
    }
}