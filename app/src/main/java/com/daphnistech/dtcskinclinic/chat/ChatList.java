package com.daphnistech.dtcskinclinic.chat;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.adapter.ChatListAdapter;
import com.daphnistech.dtcskinclinic.firebase.NotificationUtils;
import com.daphnistech.dtcskinclinic.helper.Config;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.daphnistech.dtcskinclinic.model.Doctors;
import com.daphnistech.dtcskinclinic.model.Patients;

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

public class ChatList extends Fragment {
    RecyclerView chatListRecyclerView;
    ChatListAdapter chatListAdapter;
    private List<Doctors> doctorChatList;
    private List<Patients> patientChatList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    public ChatList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
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
        chatListRecyclerView = view.findViewById(R.id.chatRecyclerView);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_STATUS_UPDATE)) {
                    updateStatus(Integer.parseInt(intent.getStringExtra("id")), intent.getStringExtra("status"), intent.getStringExtra("type"));
                } else {
                    updateCount(intent);
                }
            }
        };

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

    private void updateCount(Intent intent) {
        String type = new PreferenceManager(getActivity(), Constant.USER_DETAILS).getLoginType();
        if (type.equals(Constant.DOCTOR)) {
            int id = intent.getIntExtra("patient_id", 0);
            for (Patients patients : patientChatList) {
                if (patients.getPatientId() == id) {
                    int index = patientChatList.indexOf(patients);
                    patients.setUnreadCount(patients.getUnreadCount() + 1);
                    patientChatList.remove(index);
                    patientChatList.add(index, patients);
                    break;
                }
            }
        } else {
            int id = intent.getIntExtra("doctor_id", 0);
            for (Doctors doctors : doctorChatList) {
                if (doctors.getDoctorId() == id) {
                    int index = doctorChatList.indexOf(doctors);
                    doctors.setUnreadCount(doctors.getUnreadCount() + 1);
                    doctorChatList.remove(index);
                    doctorChatList.add(index, doctors);
                    break;
                }
            }
        }
        chatListAdapter.notifyDataSetChanged();

    }

    private void setChatList() {
        doctorChatList = new ArrayList<>();
        patientChatList = new ArrayList<>();
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
            call = api.getPatientChatList(user_id);
        } else {
            call = api.getDoctorChatList(user_id);
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
                                    if (preferenceManager.getLoginType().equals(Constant.DOCTOR)) {
                                        patientChatList.add(new Patients(doctors.getInt("appointment_id"), doctors.getInt("patient_id"), doctors.getString("name"), doctors.getString("photo"), doctors.getString("age"), doctors.getString("is_online"), doctors.getInt("unread_count")));
                                    } else {
                                        doctorChatList.add(new Doctors(doctors.getInt("appointment_id"), doctors.getInt("doctor_id"), doctors.getString("name"), doctors.getString("photo"), doctors.getString("designation"), doctors.getString("is_online"), doctors.getInt("unread_count")));
                                    }
                                }
                                chatListAdapter.notifyDataSetChanged();
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

    /**
     * Updates the chat list unread count and the last message
     */
    private void updateStatus(int id, String status, String type) {
        if (Constant.PATIENT.equalsIgnoreCase(type)) {
            for (Patients patients : patientChatList) {
                if (patients.getPatientId() == id) {
                    int index = patientChatList.indexOf(patients);
                    patients.setIsOnline(status);
                    patientChatList.remove(index);
                    patientChatList.add(index, patients);
                    break;
                }
            }
        } else {
            for (Doctors doctors : doctorChatList) {
                if (doctors.getDoctorId() == id) {
                    int index = doctorChatList.indexOf(doctors);
                    doctors.setOnline(status);
                    doctorChatList.remove(index);
                    doctorChatList.add(index, doctors);
                    break;
                }
            }
        }
        chatListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // registering the receiver for new notification
        IntentFilter filter = new IntentFilter(Config.PUSH_NOTIFICATION);
        filter.addAction(Config.PUSH_STATUS_UPDATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver,
                filter);


        setChatList();

        chatListAdapter = new ChatListAdapter(getActivity(), doctorChatList, patientChatList);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatListRecyclerView.setAdapter(chatListAdapter);

        NotificationUtils.clearNotifications();

        getView().setOnKeyListener((v, keyCode, event) -> {

            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                // handle back button's click listener
                Dialog dialog = new Dialog(getActivity());
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