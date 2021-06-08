package com.daphnistech.dtcskinclinic.home;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.adapter.AdvertViewAdapter;
import com.daphnistech.dtcskinclinic.adapter.DoctorsAdapter;
import com.daphnistech.dtcskinclinic.appointment.ConfirmAppointment;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.daphnistech.dtcskinclinic.model.Advertisement;
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

public class Home extends Fragment {
    RecyclerView advertRecyclerView, doctorsRecyclerView;
    AdvertViewAdapter advertViewAdapter;
    DoctorsAdapter doctorsAdapter;
    Context context;
    List<Advertisement> advertisementList;
    List<Doctors> doctorsList;

    public Home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
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

        initViews(view);
        setAdvert();
        setDoctors();

        advertViewAdapter = new AdvertViewAdapter(context, advertisementList);
        advertRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(advertRecyclerView);
        advertRecyclerView.setAdapter(advertViewAdapter);

        doctorsAdapter = new DoctorsAdapter(context, doctorsList, "dashboard");
        doctorsRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        SnapHelper snapHelper1 = new LinearSnapHelper();
        snapHelper1.attachToRecyclerView(doctorsRecyclerView);
        doctorsRecyclerView.setAdapter(doctorsAdapter);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                getActivity().finish();
                return true;
            }
            return false;
        });

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
                                scrollForward();
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

    private void scrollBackward() {
        final int speedScroll = 100;
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            final int count = doctorsList.size();
            @Override
            public void run() {
                /*if(count >=0){
                    doctorsRecyclerView.smoothScrollToPosition(count--);
                    handler.postDelayed(this,speedScroll);
                }*/
                doctorsRecyclerView.smoothScrollToPosition(0);
            }
        };
        handler.postDelayed(runnable,speedScroll);
    }

    private void scrollForward() {
        final int speedScroll = 500;
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int count = 0;
            @Override
            public void run() {
                if(count < doctorsList.size()){
                    doctorsRecyclerView.smoothScrollToPosition(count++);
                    handler.postDelayed(this,speedScroll);
                } else {
                    scrollBackward();
                }


            }
        };
        handler.postDelayed(runnable,speedScroll);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressWarnings("deprecation")
    private void setAdvert() {
        advertisementList = new ArrayList<>();
        advertisementList.add(new Advertisement(getResources().getDrawable(R.drawable.advertisment)));
        advertisementList.add(new Advertisement(getResources().getDrawable(R.drawable.advertisment)));
        advertisementList.add(new Advertisement(getResources().getDrawable(R.drawable.advertisment)));
    }

    private void initViews(View view) {
        context = getActivity();
        advertRecyclerView = view.findViewById(R.id.advertRecyclerView);
        doctorsRecyclerView = view.findViewById(R.id.doctorRecyclerView);
        CustomProgressBar.showProgressBar(context, false, "");
    }

    public void getCurrentFragment(FragmentActivity context) {
        assert context.getSupportFragmentManager() != null;
        context.getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ConfirmAppointment()).addToBackStack("home").commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setOnKeyListener((v, keyCode, event) -> {

            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                // handle back button's click listener
                Dialog dialog = new Dialog(context);
                // Removing the features of Normal Dialogs
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_confirm_exit);
                dialog.setCancelable(true);
                dialog.show();

                dialog.findViewById(R.id.confirm).setOnClickListener(confirm -> getActivity().finish());
                dialog.findViewById(R.id.cancel).setOnClickListener(cancel -> dialog.dismiss());

                return true;
            }
            return false;
        });
    }
}