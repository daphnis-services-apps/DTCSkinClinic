package com.daphnistech.dtcskinclinic.appointment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.activity.ConversationActivity;
import com.daphnistech.dtcskinclinic.activity.LoginActivity;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PolicyOpener;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.daphnistech.dtcskinclinic.helper.DateHelper.getCurrentTime;

public class ConfirmAppointment extends Fragment implements View.OnClickListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    CircleImageView photo;
    TextView name, designation, mode, consultationFees, submit, date, time, myAppointments, tnc;
    EditText remarks;
    PreferenceManager preferenceManager;
    ConstraintLayout checkLayout, confirmLayout;
    LottieAnimationView selectDate;
    ImageView back;

    int day, month, year, hour, minute;
    int myday, myMonth, myYear, myHour, myMinute;
    private boolean isSelected = false;
    String formattedDate, formattedTime;

    public ConfirmAppointment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirm_appointment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);
        Glide.with(getActivity()).load(preferenceManager.getDoctorPhoto()).placeholder(getActivity().getDrawable(R.drawable.doctor_plus)).into(photo);
        name.setText(String.format("Dr. %s", preferenceManager.getDoctorName()));
        designation.setText(preferenceManager.getDesignation());
        consultationFees.setText(String.format("₹ %s", preferenceManager.getConsultationFees()));
        //mode.setText(preferenceManager.getAppointmentMode());
        //time.setText(String.format("%s\n%s", getCurrentTime("date"), getCurrentTime("time")));
        /*if (preferenceManager.getAppointmentMode().equals("ONLINE")) {
            mode.setTextColor(Color.GREEN);
        } else {
            mode.setTextColor(Color.RED);
        }*/

        date.setOnClickListener(this);
        time.setOnClickListener(this);
        selectDate.setOnClickListener(this);

        tnc.setOnClickListener(v -> PolicyOpener.showPolicy(getActivity(), true, UserInterface.BASE_URL + "refund_policy.html"));
        submit.setOnClickListener(v -> {
            if (preferenceManager.getUserID() != 0) {
                if (isSelected)
                    addTransaction();
                else
                    Toast.makeText(getActivity(), "Please Select Date for Appointment", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Please Login First", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        myAppointments.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().popBackStack("myAppointments", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });

        back.setOnClickListener(v -> getFragmentManager().popBackStackImmediate());
    }

    private void addTransaction() {
        CustomProgressBar.showProgressBar(getContext(), false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call = api.addTransaction(
                new Random().nextInt(500),
                preferenceManager.getUserID(),
                preferenceManager.getDoctorId(),
                Integer.parseInt(preferenceManager.getConsultationFees()),
                getCurrentTime("date"),
                getCurrentTime("time"),
                "Success");

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (!jsonObject.getBoolean("error")) {
                                addAppointment(jsonObject.getInt("message"));
                            } else {
                                Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                CustomProgressBar.hideProgressBar();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                CustomProgressBar.hideProgressBar();
            }
        });
    }

    private void addAppointment(int payment_id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call = api.addAppointment(
                preferenceManager.getName(),
                preferenceManager.getUserID(),
                preferenceManager.getDoctorId(),
                payment_id,
                preferenceManager.getAppointmentMode(),
                formattedDate,
                formattedTime,
                "OPEN",
                remarks.getText().toString().isEmpty() ? "N/A" : remarks.getText().toString());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (!jsonObject.getBoolean("error")) {
                                int id = jsonObject.getInt("appointment_id");
                                checkLayout.setVisibility(View.GONE);
                                confirmLayout.setVisibility(View.VISIBLE);
                                new Handler().postDelayed(() -> getActivity().startActivity(
                                        new Intent(getActivity(), ConversationActivity.class)
                                                .putExtra("appointment_id", id)
                                                .putExtra("name", preferenceManager.getDoctorName())
                                                .putExtra("receiver_id", preferenceManager.getDoctorId())
                                                .putExtra("appointment_status","open")
                                                .putExtra("is_online", false)
                                                .putExtra("unread_count", 0)
                                ), 3000);
                            } else {
                                Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                            CustomProgressBar.hideProgressBar();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                CustomProgressBar.hideProgressBar();
            }
        });
    }

    private void initViews(View view) {
        photo = view.findViewById(R.id.photo);
        name = view.findViewById(R.id.name);
        designation = view.findViewById(R.id.designation);
        mode = view.findViewById(R.id.mode);
        consultationFees = view.findViewById(R.id.fees);
        remarks = view.findViewById(R.id.remarks);
        submit = view.findViewById(R.id.submit);
        date = view.findViewById(R.id.date);
        selectDate = view.findViewById(R.id.selectDate);
        time = view.findViewById(R.id.time);
        checkLayout = view.findViewById(R.id.checkLayout);
        confirmLayout = view.findViewById(R.id.confirmLayout);
        myAppointments = view.findViewById(R.id.history);
        tnc = view.findViewById(R.id.tnc);
        back = view.findViewById(R.id.back);
    }

    @Override
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        myYear = year;
        myday = dayOfMonth;
        myMonth = month;
        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR);
        minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute, false);
        timePickerDialog.show();
    }

    @SuppressLint({"DefaultLocale", "SimpleDateFormat"})
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        myHour = hourOfDay;
        myMinute = minute;
        try {
            formattedDate = String.format("%d-%s-%d", myday, new DateFormatSymbols().getShortMonths()[myMonth], myYear);
            formattedTime = new SimpleDateFormat("hh:mm aa").format(new SimpleDateFormat("HH:mm").parse(myHour + ":" + myMinute));
        } catch (ParseException e) {
            formattedTime = String.valueOf(myHour);
        }
        time.setText(String.format("%s\n%s", formattedDate, formattedTime));
        isSelected = true;
    }
}