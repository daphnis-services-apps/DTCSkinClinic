package com.daphnistech.dtcskinclinic.patient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.activity.AddPatient;
import com.daphnistech.dtcskinclinic.activity.PatientDashboard;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.SavingBar;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ConfirmDetails extends Fragment {
    TextView name, age, mobile, address, gender, doctorName, designation, rating;
    ImageView diabetes, hyperTension, thyroidProblem, drugAllergy, skinDisease, cosmetology, hairProblem, sexDisease;
    PreferenceManager preferenceManager;
    String token;
    int loop = 0;
    private CheckBox checkBox;
    private TextView next;
    private ProgressBar progressBar;

    public ConfirmDetails() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirm_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }
        initViews(view);
        settingValues();

        next.setOnClickListener(v -> {
            if (checkBox.isChecked()) {
                Toast.makeText(getActivity(), "Saving Patient's Details", Toast.LENGTH_SHORT).show();
                loginPatient(getActivity());
            } else {
                Toast.makeText(getActivity(), "Please Confirm yours Details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loginPatient(Context context) {
        SavingBar.showProgressBar(context, false);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(context, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                SavingBar.hideProgressBar();
                return;
            }
            // Get new FCM registration token
            token = task.getResult();
            preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);
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
            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), preferenceManager.getName());
            RequestBody age = RequestBody.create(MediaType.parse("text/plain"), preferenceManager.getAge());
            RequestBody gender = RequestBody.create(MediaType.parse("text/plain"), preferenceManager.getGender());
            RequestBody address = RequestBody.create(MediaType.parse("text/plain"), preferenceManager.getAddress().isEmpty() ? "N/A" : preferenceManager.getAddress());
            RequestBody state = RequestBody.create(MediaType.parse("text/plain"), preferenceManager.getState().isEmpty() ? "N/A" : preferenceManager.getState());
            RequestBody pin = RequestBody.create(MediaType.parse("text/plain"), preferenceManager.getPIN().isEmpty() ? "N/A" : preferenceManager.getPIN());
            RequestBody isOnline = RequestBody.create(MediaType.parse("text/plain"), preferenceManager.isOnline());
            RequestBody fcmToken = RequestBody.create(MediaType.parse("text/plain"), token);
            @SuppressLint("HardwareIds") RequestBody deviceToken = RequestBody.create(MediaType.parse("text/plain"), Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            //getting API Call
            @SuppressLint("HardwareIds") Call<String> call = api.loginPatient(
                    name,
                    mobile,
                    age,
                    gender,
                    address,
                    state,
                    pin,
                    isOnline,
                    fcmToken,
                    deviceToken,
                    image);

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
                                    Toast.makeText(context, "Patients Details Saved, Saving Patient's Diseases....", Toast.LENGTH_SHORT).show();
                                    setPreferences(jsonObject.getJSONObject("user") , context);
                                } else {
                                    Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                    SavingBar.hideProgressBar();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                SavingBar.hideProgressBar();
                            }

                        } else {
                            Log.i("onEmptyResponse", "Returned empty response");
                            Toast.makeText(context, "Returned empty response", Toast.LENGTH_SHORT).show();
                            SavingBar.hideProgressBar();
                        }
                    } else if (response.errorBody() != null) {
                        //On Error Body Response(email error)
                        Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                        SavingBar.hideProgressBar();
                    }

                }

                @Override
                public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                    Toast.makeText(getActivity(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    SavingBar.hideProgressBar();
                }
            });
        });
    }

    private void setPreferences(JSONObject user , Context context) throws JSONException {
        preferenceManager.setUserID(user.getInt("patient_id"));
        preferenceManager.setName(user.getString("name"));
        preferenceManager.setMobile(user.getString("mobile"));
        preferenceManager.setAge(user.getString("age"));
        preferenceManager.setGender(user.getString("gender"));
        preferenceManager.setAddress(user.getString("address"));
        preferenceManager.setState(user.getString("state"));
        preferenceManager.setPIN(user.getString("pin"));
        saveCurrentDiseases(context);
    }

    private void savePreviousDiseases() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        //getting API Call
        Call<String> call = api.addPreviousDisease(
                preferenceManager.getUserID(),
                preferenceManager.isDiabetes(),
                preferenceManager.isHyperTension(),
                preferenceManager.isThyroidProblem(),
                preferenceManager.isDrugAllergy(),
                preferenceManager.getDrugRemarks().equals("") ? "N/A" : preferenceManager.getDrugRemarks());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Previous Disease Saved, Saving Current Diseases...., Please wait for a while,saving files make take time Depending on your Disease's Image's Resolution and internet speed", Toast.LENGTH_LONG).show();
                    saveCurrentDiseases(getActivity());
                } else if (response.errorBody() != null) {
                    Toast.makeText(getActivity(), response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                    SavingBar.hideProgressBar();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getActivity(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                SavingBar.hideProgressBar();
            }
        });
    }

    private void saveCurrentDiseases(Context context) {
        List<String> diseasesArray = new ArrayList<>();
        int user_id = preferenceManager.getUserID();
        if (preferenceManager.isSkinDisease())
            diseasesArray.add(Constant.SKIN_DISEASE);
        if (preferenceManager.isCosmetology())
            diseasesArray.add(Constant.COSMETOLOGY);
        if (preferenceManager.isHairProblem())
            diseasesArray.add(Constant.HAIR_PROBLEM);
        if (preferenceManager.isSexDisease())
            diseasesArray.add(Constant.SEX_DISEASE);

        forwardLoop(diseasesArray, user_id, context);
    }

    private void forwardLoop(List<String> diseasesArray, int user_id, Context context) {
        SavingBar.hideProgressBar();
        SavingBar.showProgressBar(context, false);
        if (loop == diseasesArray.size()) {
            preferenceManager.setLoggedIn(true);
            context.startActivity(new Intent(context, PatientDashboard.class));
            ((AddPatient) context).finish();
            return;
        }
        int count = loop + 1;
        Toast.makeText(context, "Saving " + count + " out of " + diseasesArray.size() + "," + diseasesArray.get(loop) + " Details", Toast.LENGTH_SHORT).show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);
        PreferenceManager preferenceManager = null;
        if (diseasesArray.get(loop).equals(Constant.SKIN_DISEASE)) {
            preferenceManager = new PreferenceManager(context, Constant.SKIN_DISEASE);
        }
        if (diseasesArray.get(loop).equals(Constant.COSMETOLOGY)) {
            preferenceManager = new PreferenceManager(context, Constant.COSMETOLOGY);
        }
        if (diseasesArray.get(loop).equals(Constant.HAIR_PROBLEM)) {
            preferenceManager = new PreferenceManager(context, Constant.HAIR_PROBLEM);
        }
        if (diseasesArray.get(loop).equals(Constant.SEX_DISEASE)) {
            preferenceManager = new PreferenceManager(context, Constant.SEX_DISEASE);
        }
        MultipartBody.Part pic1;
        if (!preferenceManager.getPic1().equals("")) {
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
        if (!preferenceManager.getPic2().equals("")) {
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
        if (!preferenceManager.getPic3().equals("")) {
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
        if (!preferenceManager.getPDF().equals("")) {
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
        if (!preferenceManager.getAffectedArea().equals("")) {
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
        String diseaseName = diseasesArray.get(loop);
        String oldAge = preferenceManager.getOldAge();
        String diseaseType = preferenceManager.getDiseaseType();
        String commentsString = preferenceManager.getComments().equals("") ? "N/A" : preferenceManager.getComments();
        String subProblem = preferenceManager.getSubProblem().equals("") ? "N/A" : preferenceManager.getSubProblem();
        RequestBody patient_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(user_id));
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
                        loop++;
                        Toast.makeText(context, diseaseName + " details saved", Toast.LENGTH_SHORT).show();
                        forwardLoop(diseasesArray, user_id, context);
                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(context, "Returned empty response", Toast.LENGTH_SHORT).show();
                        SavingBar.hideProgressBar();
                    }
                } else if (response.errorBody() != null) {
                    Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                    SavingBar.hideProgressBar();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                SavingBar.hideProgressBar();
            }
        });
    }

    @SuppressLint({"UseCompatLoadingForDrawables"})
    @SuppressWarnings("deprecation")
    private void settingValues() {
        preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);
        name.setText(preferenceManager.getName());
        age.setText(preferenceManager.getAge());
        mobile.setText(preferenceManager.getMobile());
        gender.setText(preferenceManager.getGender());
        address.setText(String.format("%s, %s, %s", preferenceManager.getAddress(), preferenceManager.getState(), preferenceManager.getPIN()));
        if (preferenceManager.isDiabetes()) {
            diabetes.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
        }
        if (preferenceManager.isHyperTension()) {
            hyperTension.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
        }
        if (preferenceManager.isThyroidProblem()) {
            thyroidProblem.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
        }
        if (preferenceManager.isDrugAllergy()) {
            drugAllergy.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
        }
        if (preferenceManager.isSkinDisease()) {
            skinDisease.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
        }
        if (preferenceManager.isCosmetology()) {
            cosmetology.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
        }
        if (preferenceManager.isHairProblem()) {
            hairProblem.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
        }
        if (preferenceManager.isSexDisease()) {
            sexDisease.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_circle_24));
        }
        doctorName.setText(preferenceManager.getDoctorName());
        designation.setText(preferenceManager.getDesignation());
        rating.setText(preferenceManager.getRating());
    }

    private void initViews(View view) {
        name = view.findViewById(R.id.name);
        age = view.findViewById(R.id.age);
        mobile = view.findViewById(R.id.mobile);
        gender = view.findViewById(R.id.gender);
        address = view.findViewById(R.id.address);
        doctorName = view.findViewById(R.id.doctorName);
        designation = view.findViewById(R.id.designation);
        rating = view.findViewById(R.id.rating);
        diabetes = view.findViewById(R.id.diabetesImage);
        hyperTension = view.findViewById(R.id.hyperTensionImage);
        thyroidProblem = view.findViewById(R.id.thyroidProblemImage);
        drugAllergy = view.findViewById(R.id.drugAllergyImage);
        skinDisease = view.findViewById(R.id.skinDiseaseImage);
        cosmetology = view.findViewById(R.id.cosmetologyImage);
        hairProblem = view.findViewById(R.id.hairProblemImage);
        sexDisease = view.findViewById(R.id.sexProblemImage);
        checkBox = view.findViewById(R.id.checkbox);
        next = view.findViewById(R.id.submit);
        progressBar = view.findViewById(R.id.progressBar);
    }
}