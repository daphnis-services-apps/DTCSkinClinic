package com.daphnistech.dtcskinclinic.appointment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.adapter.DoctorsAdapter;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.daphnistech.dtcskinclinic.model.Doctors;

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

public class ChooseDoctorForAppointment extends Fragment {
    RecyclerView doctorsRecyclerView;
    DoctorsAdapter doctorsAdapter;
    List<Doctors> doctorsList;
    ImageView back;
    PreferenceManager preferenceManager;

    public ChooseDoctorForAppointment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_doctor_for_appointment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setDoctors();
        back.setOnClickListener(v -> getFragmentManager().popBackStackImmediate());
        preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);
        doctorsAdapter = new DoctorsAdapter(getActivity(), doctorsList, "appointment_choose");
        doctorsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        doctorsRecyclerView.setAdapter(doctorsAdapter);

    }

    private void initViews(View view) {
        doctorsRecyclerView = view.findViewById(R.id.doctorRecyclerView);
        back = view.findViewById(R.id.back);
        CustomProgressBar.showProgressBar(getActivity(),false);
    }

    private void setDoctors() {
        doctorsList = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call = api.getAllDoctors();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (!jsonObject.getBoolean("error")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("doctors");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject doctors = new JSONObject(jsonArray.getString(i));
                                    doctorsList.add(new Doctors(doctors.getInt("doctor_id"), doctors.getString("name"), doctors.getString("photo"), doctors.getString("designation"), "4.5", doctors.getString("consultation_fees")));
                                }
                                doctorsAdapter.notifyDataSetChanged();
                                CustomProgressBar.hideProgressBar();
                            } else {
                                Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                CustomProgressBar.hideProgressBar();
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
                    Toast.makeText(getActivity(), response.message(), Toast.LENGTH_SHORT).show();
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

    public void getCurrentFragment(FragmentActivity context){
        assert context.getSupportFragmentManager() != null;
        context.getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ConfirmAppointment()).addToBackStack("chooseDoctor").commit();
    }
}