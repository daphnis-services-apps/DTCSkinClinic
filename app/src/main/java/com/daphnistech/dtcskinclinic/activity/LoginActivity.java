package com.daphnistech.dtcskinclinic.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PolicyOpener;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import in.aabhasjindal.otptextview.OTPListener;
import in.aabhasjindal.otptextview.OtpTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Login Activity";
    EditText mobile;
    ImageView verifyIcon;
    ProgressBar progressBar, progressBarOTP;
    TextView submit, submitOTP, confirmText, resendOTP, tnc, tos, withoutLogin;
    ConstraintLayout mobileLayout, OTPLayout;
    OtpTextView otpTextView;
    Context context;
    PreferenceManager preferenceManager;
    String token;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.grey));
        }
        initViews();
        context = LoginActivity.this;
        preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);
        mAuth = FirebaseAuth.getInstance();

        submit.setOnClickListener(this);
        submitOTP.setOnClickListener(this);
        resendOTP.setOnClickListener(this);
        tnc.setOnClickListener(this);
        tos.setOnClickListener(this);
        withoutLogin.setOnClickListener(this);

        mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 10) {
                    verifyIcon.setVisibility(View.VISIBLE);
                } else {
                    verifyIcon.setVisibility(View.GONE);
                }
            }
        });

        otpTextView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {
                // fired when user types something in the Otp box
            }

            @Override
            public void onOTPComplete(String otp) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                signInWithPhoneAuthCredential(credential);
                //updateUser();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                otpTextView.setOTP(credential.getSmsCode());
                //signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                mobileLayout.setVisibility(View.VISIBLE);
                OTPLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                submit.setVisibility(View.VISIBLE);

                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(context, "Code has been send successfully", Toast.LENGTH_SHORT).show();
                confirmText.setText(String.format("We have sent you an SMS on +91 %s with 6 digit verification code.", mobile.getText().toString()));
                mobileLayout.setVisibility(View.GONE);
                OTPLayout.setVisibility(View.VISIBLE);
                otpTextView.requestFocusOTP();
            }
        };

    }

    private void initViews() {
        mobile = findViewById(R.id.mobile);
        verifyIcon = findViewById(R.id.verifyIcon);
        submit = findViewById(R.id.submit);
        submitOTP = findViewById(R.id.submitOTP);
        progressBar = findViewById(R.id.progressBar);
        progressBarOTP = findViewById(R.id.progressBarOTP);
        mobileLayout = findViewById(R.id.mobileLayout);
        OTPLayout = findViewById(R.id.submitOtpLayout);
        otpTextView = findViewById(R.id.otp_view);
        confirmText = findViewById(R.id.confirmText);
        resendOTP = findViewById(R.id.resendOTP);
        withoutLogin = findViewById(R.id.withoutLogin);
        tnc = findViewById(R.id.tnc);
        tos = findViewById(R.id.tos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onClick(View v) {
        if (v == submit) {
            if (mobile.getText().length() == 10) {
               /* PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(mAuth)
                                .setPhoneNumber("+91" + mobile.getText().toString())       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(this)                 // Activity (for callback binding)
                                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                .build();
                PhoneAuthProvider.verifyPhoneNumber(options);*/
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                submit.setVisibility(View.GONE);
                withoutLogin.setVisibility(View.GONE);
                new Handler().postDelayed(() -> {
                }, 1500);
                updateUser();
            } else {
                Toast.makeText(this, "Enter Valid Mobile Number", Toast.LENGTH_LONG).show();
            }
        } else if (v == submitOTP) {
            if (otpTextView.getOTP().length() != 6) {
                Toast.makeText(this, "Enter Valid OTP", Toast.LENGTH_LONG).show();
            }
        } else if (v == resendOTP) {
            resendVerificationCode();
        } else if (v == tnc) {
            PolicyOpener.showPolicy(context,true, UserInterface.BASE_URL + "privacy_policy.html");
        } else if (v == tos) {
            PolicyOpener.showPolicy(context, true, UserInterface.BASE_URL + "terms_of_services.html");
        } else if (v == withoutLogin) {
            preferenceManager.setLoginSkipped(true);
            if (preferenceManager.getLoginType().equals(Constant.PATIENT)) {
                startActivity(new Intent(context, AddPatient.class));
            } else {
                startActivity(new Intent(context, AddDoctor.class));
            }
            finish();
        }
    }

    private void checkUser() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);
        //getting API Call
        Call<String> call;
        if (preferenceManager.getLoginType().equals(Constant.PATIENT)) {
            call = api.isPatientExists(mobile.getText().toString(), token, Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        } else {
            call = api.isDoctorExists(mobile.getText().toString(), token, Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        }

        //API Call Response
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    //getting Response Body
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (jsonObject.getBoolean("isExists")) {
                                setPreferences(jsonObject.getJSONObject("user"));
                            } else {
                                addUser();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            progressBarOTP.setVisibility(View.GONE);
                            submitOTP.setVisibility(View.VISIBLE);
                        }
                        //Parsing the JSON

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(context, "Returned empty Response", Toast.LENGTH_SHORT).show();
                        progressBarOTP.setVisibility(View.GONE);
                        submitOTP.setVisibility(View.VISIBLE);
                    }
                } else if (response.errorBody() != null) {
                    //On Error Body Response(email error)
                    Toast.makeText(LoginActivity.this, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                    progressBarOTP.setVisibility(View.GONE);
                    submitOTP.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(LoginActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                submit.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addUser() {
        if (preferenceManager.getLoginType().equals(Constant.PATIENT)) {
            Intent intent = new Intent(LoginActivity.this, AddPatient.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(LoginActivity.this, AddDoctor.class);
            startActivity(intent);
        }
        finish();
    }

    private void setPreferences(JSONObject user) throws JSONException {
        //JSONArray user = new JSONArray(object);

        preferenceManager.setName(user.getString(Constant.NAME));
        preferenceManager.setMobile(user.getString(Constant.MOBILE));
        preferenceManager.setLoggedIn(true);
        if (new PreferenceManager(context, Constant.USER_DETAILS).getLoginType().equals(Constant.PATIENT)) {
            preferenceManager.setUserID(user.getInt(Constant.PATIENT_ID));
            preferenceManager.setFirstTimeLogin(false);
            startActivity(new Intent(context, PatientDashboard.class));
        } else {
            preferenceManager.setUserID(user.getInt(Constant.DOCTOR_ID));
            preferenceManager.setApproved(user.getBoolean(Constant.IS_APPROVED));
            startActivity(new Intent(context, DoctorDashboard.class));
        }
        finish();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressBarOTP.setIndeterminate(true);
        progressBarOTP.setVisibility(View.VISIBLE);
        submitOTP.setVisibility(View.GONE);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        FirebaseUser user = task.getResult().getUser();
                        // Update UI
                        updateUser();
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            Toast.makeText(context, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                        progressBarOTP.setVisibility(View.GONE);
                        submitOTP.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void updateUser() {
        preferenceManager.setMobile(mobile.getText().toString());
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task1 -> {
            if (!task1.isSuccessful()) {
                Toast.makeText(context, task1.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            // Get new FCM registration token
            token = task1.getResult();
            checkUser();
        });
    }

    private void resendVerificationCode() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + mobile.getText().toString())       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(mResendToken)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    @Override
    public void onBackPressed() {

        if (OTPLayout.getVisibility() == View.VISIBLE) {
            mobileLayout.setVisibility(View.VISIBLE);
            submit.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            OTPLayout.setVisibility(View.GONE);
        } else {
            finish();
        }
    }
}