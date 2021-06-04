package com.daphnistech.dtcskinclinic.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.adapter.DoctorsAdapter;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.model.Doctors;

import java.util.ArrayList;
import java.util.List;

public class ChooseDoctor extends Fragment {
    RecyclerView doctorsRecyclerView;
    DoctorsAdapter doctorsAdapter;
    List<Doctors> doctorsList;
    TextView next;
    ImageView back;
    PreferenceManager preferenceManager;
    CheckBox checkBox;

    public ChooseDoctor() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_doctor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setDoctors();
        preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);
        doctorsAdapter = new DoctorsAdapter(getActivity(), doctorsList, "choose");
        doctorsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        doctorsRecyclerView.setAdapter(doctorsAdapter);

        next.setOnClickListener(v -> {
            List<String> doctor = doctorsAdapter.getDoctor();
            if (doctor.size() == 0){
                Toast.makeText(getActivity(), "Please choose one of the Doctor", Toast.LENGTH_SHORT).show();
            } else {
                preferenceManager.setDoctorName(doctor.get(0));
                preferenceManager.setDesignation(doctor.get(1));
                preferenceManager.setRating(doctor.get(2));
                assert getActivity().getSupportFragmentManager() != null;
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new PreviousDiseases()).addToBackStack("doctor").commit();
            }
        });
    }

    private void initViews(View view) {
        doctorsRecyclerView = view.findViewById(R.id.doctorRecyclerView);
        next = view.findViewById(R.id.submit);
        back = view.findViewById(R.id.back);
    }

    private void setDoctors() {
        doctorsList = new ArrayList<>();
        /*doctorsList.add(new Doctors("Dr. Rohit Goel","B.Sc, MBBS, DDVL, MD- Dermitologist","4.5"));
        doctorsList.add(new Doctors("Dr. Vikas Mishra","B.Sc, MBBS, DDVL, MD- Dermitologist","4.2"));
        doctorsList.add(new Doctors("Dr. Rishabh Batra","B.Sc, MBBS, DDVL, MD- Dermitologist","3.9"));
        doctorsList.add(new Doctors("Dr. Sneha Gupta","B.Sc, MBBS, DDVL, MD- Dermitologist","4.3"));*/
    }

}