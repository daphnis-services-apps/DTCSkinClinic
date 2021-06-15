package com.daphnistech.dtcskinclinic.appointment;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.adapter.AppointmentAdapter;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.daphnistech.dtcskinclinic.model.Appointments;

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

public class MyAppointments extends Fragment {
    RecyclerView myAppointmentRecyclerView;
    AppointmentAdapter appointmentAdapter;
    List<Appointments> appointmentList;
    TextView noMessage;
    LottieAnimationView animationView;
    private TextView next;
    private ImageView back;

    public MyAppointments() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_appointments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getActivity().getWindow().getDecorView().setSystemUiVisibility(getActivity().getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }
        myAppointmentRecyclerView = view.findViewById(R.id.chatRecyclerView);
        noMessage = view.findViewById(R.id.noMessage);
        next = view.findViewById(R.id.submit);
        animationView = view.findViewById(R.id.animationView);
        back = view.findViewById(R.id.back);
        if (new PreferenceManager(getActivity(), Constant.USER_DETAILS).getLoginType().equals(Constant.DOCTOR))
            next.setVisibility(View.GONE);
        setAppointmentsList();
        appointmentAdapter = new AppointmentAdapter(getActivity(), appointmentList);
        myAppointmentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myAppointmentRecyclerView.setAdapter(appointmentAdapter);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert getActivity().getSupportFragmentManager() != null;
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChooseDoctorForAppointment()).addToBackStack("myAppointments").commit();
            }
        });

        back.setOnClickListener(v -> getFragmentManager().popBackStackImmediate());
    }

    private void setAppointmentsList() {
        CustomProgressBar.showProgressBar(getActivity(), false);
        appointmentList = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call;
        if (new PreferenceManager(getActivity(), Constant.USER_DETAILS).getLoginType().equals(Constant.PATIENT))
            call = api.getPatientAppointments(new PreferenceManager(getActivity(), Constant.USER_DETAILS).getUserID());
        else
            call = api.getDoctorAppointments(new PreferenceManager(getActivity(), Constant.USER_DETAILS).getUserID());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (!jsonObject.getBoolean("error")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("appointments");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject appointments = new JSONObject(jsonArray.getString(i));
                                    appointmentList.add(new Appointments(appointments.getInt("id"), appointments.getString("name"), appointments.getString("body"), appointments.getString("photo"), appointments.getInt("transaction_amount"), appointments.getString("appointment_date"), appointments.getString("appointment_time"), appointments.getInt("appointment_id"), appointments.getString("appointment_mode"), appointments.getString("appointment_status")));
                                }
                                appointmentAdapter.notifyDataSetChanged();
                                CustomProgressBar.hideProgressBar();
                                if (appointmentList.size() > 0) {
                                    animationView.setVisibility(View.GONE);
                                    noMessage.setVisibility(View.GONE);
                                }
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
}