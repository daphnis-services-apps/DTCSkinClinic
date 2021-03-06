package com.daphnistech.dtcskinclinic.account;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.activity.DoctorDashboard;
import com.daphnistech.dtcskinclinic.activity.LoginActivity;
import com.daphnistech.dtcskinclinic.activity.LoginChooser;
import com.daphnistech.dtcskinclinic.activity.MyPatientDoctorList;
import com.daphnistech.dtcskinclinic.activity.PatientDashboard;
import com.daphnistech.dtcskinclinic.activity.ViewDoctorDetails;
import com.daphnistech.dtcskinclinic.activity.ViewPatientDetails;
import com.daphnistech.dtcskinclinic.appointment.MyAppointments;
import com.daphnistech.dtcskinclinic.chat.ChatList;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.MyApplication;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.daphnistech.dtcskinclinic.transaction.TransactionsList;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MyAccount extends Fragment {
    TextView name, mobile, transactions, doctors, profile, approvalText;
    LinearLayout myAppointment, logout, myTransactions, myAccount, healthStatus, myDoctors, myChat;
    CircleImageView photo;
    View logoutView, paymentView;

    public MyAccount() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_account, container, false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.grey));
        }
        name = view.findViewById(R.id.name);
        mobile = view.findViewById(R.id.mobile);
        photo = view.findViewById(R.id.photo);
        approvalText = view.findViewById(R.id.approvalText);
        myAppointment = view.findViewById(R.id.myAppointmentsLayout);
        logout = view.findViewById(R.id.logOutLayout);
        logoutView = view.findViewById(R.id.logoutView);
        paymentView = view.findViewById(R.id.paymentView);
        transactions = view.findViewById(R.id.transactions);
        doctors = view.findViewById(R.id.doctors);
        profile = view.findViewById(R.id.profileLogin);
        myTransactions = view.findViewById(R.id.transactionLayout);
        healthStatus = view.findViewById(R.id.diseaseLayout);
        myAccount = view.findViewById(R.id.profileLayout);
        myDoctors = view.findViewById(R.id.myDoctorsLayout);
        myChat = view.findViewById(R.id.myChatLayout);

        if (new PreferenceManager(getActivity(), Constant.USER_DETAILS).getUserID() == 0) {
            profile.setText("Login");
            logout.setVisibility(View.GONE);
            logoutView.setVisibility(View.GONE);
            paymentView.setVisibility(View.GONE);
            new PreferenceManager(getActivity(), Constant.USER_DETAILS).setName("Hello Guest");
        }

        if (new PreferenceManager(getActivity(), Constant.USER_DETAILS).getLoginType().equals(Constant.DOCTOR)) {
            healthStatus.setVisibility(View.GONE);
            doctors.setText("My Patients");
            transactions.setText("My Payments");
            if (!new PreferenceManager(getActivity(), Constant.USER_DETAILS).isApproved())
                approvalText.setVisibility(View.VISIBLE);
        }

        myAppointment.setOnClickListener(v -> {
            assert getActivity().getSupportFragmentManager() != null;
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MyAppointments()).addToBackStack("myAccount").commit();
        });

        logout.setOnClickListener(v -> {
            putStatus();
        });

        myTransactions.setOnClickListener(v -> {
            assert getActivity().getSupportFragmentManager() != null;
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new TransactionsList()).addToBackStack("myAccount").commit();
        });

        myDoctors.setOnClickListener(v -> {
            assert getActivity().getSupportFragmentManager() != null;
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MyPatientDoctorList()).addToBackStack("myAccount").commit();

        });

        myChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert getActivity().getSupportFragmentManager() != null;
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChatList()).addToBackStack("myAccount").commit();
                if (new PreferenceManager(getActivity(), Constant.USER_DETAILS).getLoginType().equals(Constant.PATIENT))
                    ((PatientDashboard) getActivity()).setBottomSelected();
                else
                    ((DoctorDashboard) getActivity()).setBottomSelected();
            }
        });

        myAccount.setOnClickListener(v -> {
            if (new PreferenceManager(getActivity(), Constant.USER_DETAILS).getUserID() != 0) {
                if (new PreferenceManager(getActivity(), Constant.USER_DETAILS).getLoginType().equals(Constant.PATIENT))
                    startActivity(new Intent(getActivity(), ViewPatientDetails.class).putExtra("id", new PreferenceManager(getActivity(), Constant.USER_DETAILS).getUserID()));
                else
                    startActivity(new Intent(getActivity(), ViewDoctorDetails.class).putExtra("id", new PreferenceManager(getActivity(), Constant.USER_DETAILS).getUserID()));
            } else {
                new PreferenceManager(getActivity(), Constant.USER_DETAILS).setProfileImage("");
                new PreferenceManager(getActivity(), Constant.USER_DETAILS).setName("");
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        healthStatus.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Panel is under construction", Toast.LENGTH_SHORT).show();
        });
    }

    private void putStatus() {
        CustomProgressBar.showProgressBar(getActivity(), false);
        PreferenceManager preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);
        int id = preferenceManager.getUserID();
        String type = preferenceManager.getLoginType();
        boolean status = Constant.OFFLINE;

        Call<String> call = api.putStatus(id, type, String.valueOf(status));

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (jsonObject.getBoolean("error")) {
                                Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                CustomProgressBar.hideProgressBar();
                            } else {
                                PreferenceManager.clearAll(getActivity());
                                CustomProgressBar.hideProgressBar();
                                new PreferenceManager(getActivity(), Constant.USER_DETAILS).setVisited(true);
                                getActivity().startActivity(new Intent(getActivity(), LoginChooser.class));
                                getActivity().finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            CustomProgressBar.hideProgressBar();
                        }
                        //Parsing the JSON

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(getActivity(), "Returned empty response", Toast.LENGTH_SHORT).show();
                        CustomProgressBar.hideProgressBar();
                    }

                } else if (response.errorBody() != null) {
                    Toast.makeText(getActivity(), response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                    CustomProgressBar.hideProgressBar();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(getActivity(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                CustomProgressBar.hideProgressBar();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Glide.with(this).load(new PreferenceManager(getActivity(), Constant.USER_DETAILS).getProfileImage()).placeholder(R.drawable.diagnose).into(photo);
        name.setText(new PreferenceManager(getActivity(), Constant.USER_DETAILS).getName());
        mobile.setText(new PreferenceManager(getActivity(), Constant.USER_DETAILS).getMobile());

        MyApplication.exitOnBackPressed(getView(), getActivity());
    }


}