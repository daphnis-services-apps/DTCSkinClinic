package com.daphnistech.dtcskinclinic.appointment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectAppointmentDate extends Fragment implements View.OnClickListener {
    CircleImageView photo, continueBook;
    TextView name, designation, offlineBook, onlineBook;
    boolean checkOffline = false, checkOnline = false;
    PreferenceManager preferenceManager;
    ImageView back;

    public SelectAppointmentDate() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_appointment_date, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);
        name.setText(String.format("Dr. %s", preferenceManager.getDoctorName()));
        designation.setText(preferenceManager.getDesignation());

        back.setOnClickListener(v -> getFragmentManager().popBackStackImmediate());
        continueBook.setOnClickListener(this);
        offlineBook.setOnClickListener(this);
        onlineBook.setOnClickListener(this);
    }

    private void initViews(View view) {
        photo = view.findViewById(R.id.photo);
        name = view.findViewById(R.id.name);
        designation = view.findViewById(R.id.designation);
        offlineBook = view.findViewById(R.id.offlineBook);
        onlineBook = view.findViewById(R.id.onlineBook);
        continueBook = view.findViewById(R.id.continueBook);
        back = view.findViewById(R.id.back);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.continueBook) {
            if (checkOnline) {
                preferenceManager.setAppointmentMode("ONLINE");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ConfirmAppointment()).addToBackStack("selectDate").commit();
            } else if (checkOffline) {
                preferenceManager.setAppointmentMode("OFFLINE");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ConfirmAppointment()).addToBackStack("selectDate").commit();
            } else {
                Toast.makeText(getActivity(), "Please select any one mode", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.offlineBook) {
            if (checkOffline) {
                offlineBook.setBackground(getActivity().getResources().getDrawable(R.drawable.textview_round));
                checkOffline = false;
            } else {
                offlineBook.setBackground(getActivity().getResources().getDrawable(R.drawable.text_view_round_selected));
                onlineBook.setBackground(getActivity().getResources().getDrawable(R.drawable.textview_round));
                checkOffline = true;
                checkOnline = false;
            }
        } else if (v.getId() == R.id.onlineBook) {
            if (checkOnline) {
                onlineBook.setBackground(getActivity().getResources().getDrawable(R.drawable.textview_round));
                checkOnline = false;
            } else {
                onlineBook.setBackground(getActivity().getResources().getDrawable(R.drawable.text_view_round_selected));
                offlineBook.setBackground(getActivity().getResources().getDrawable(R.drawable.textview_round));
                checkOnline = true;
                checkOffline = false;
            }
        }
    }
}