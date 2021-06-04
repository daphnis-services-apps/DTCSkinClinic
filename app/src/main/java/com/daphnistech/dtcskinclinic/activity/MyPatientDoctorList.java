package com.daphnistech.dtcskinclinic.activity;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.adapter.MyPatientDoctorAdapter;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.daphnistech.dtcskinclinic.model.MyPatientDoctor;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MyPatientDoctorList extends Fragment {
    RecyclerView myPatientDoctorRecyclerView;
    MyPatientDoctorAdapter myPatientDoctorAdapter;
    List<MyPatientDoctor> myPatientDoctorList;

    public MyPatientDoctorList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_patient_doctor_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getActivity().getWindow().getDecorView().setSystemUiVisibility(getActivity().getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }
        myPatientDoctorRecyclerView = view.findViewById(R.id.myPatientDoctorRecyclerView);
    }

    private void setList() {
        myPatientDoctorList = new ArrayList<>();
        CustomProgressBar.showProgressBar(getActivity(), false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        PreferenceManager preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);
        int user_id = preferenceManager.getUserID();
        Call<String> call = null;
        if (preferenceManager.getLoginType().equals(Constant.PATIENT)) {
            call = api.getDoctorsList(user_id);
        } else {
            call = api.getPatientsList(user_id);
        }

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (!jsonObject.getBoolean("error")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("chatList");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject doctors = new JSONObject(jsonArray.getString(i));
                                    myPatientDoctorList.add(new MyPatientDoctor(doctors.getInt("id"), doctors.getInt("appointment_id"), doctors.getString("name"), doctors.getString("title"), doctors.getString("appointment_status")));
                                }
                                myPatientDoctorAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(getActivity(), "Returned empty response", Toast.LENGTH_LONG).show();
                    }

                } else if (response.errorBody() != null) {
                    Toast.makeText(getActivity(), response.errorBody().toString(), Toast.LENGTH_LONG).show();
                }
                CustomProgressBar.hideProgressBar();
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(getActivity(), t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                CustomProgressBar.hideProgressBar();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setList();

        myPatientDoctorAdapter = new MyPatientDoctorAdapter(getActivity(), myPatientDoctorList);
        myPatientDoctorRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myPatientDoctorRecyclerView.setAdapter(myPatientDoctorAdapter);
    }
}