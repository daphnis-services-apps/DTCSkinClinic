package com.daphnistech.dtcskinclinic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.appointment.ChooseDoctorForAppointment;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.home.Home;
import com.daphnistech.dtcskinclinic.model.Doctors;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorsAdapter extends RecyclerView.Adapter<DoctorsAdapter.ViewHolder> {
    Context context;
    List<Doctors> doctorsList;
    String type;
    private int row_index = -1;

    public DoctorsAdapter(Context context, List<Doctors> doctorsList, String type) {
        this.context = context;
        this.doctorsList = doctorsList;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = null;
        switch (type) {
            case "dashboard":
                listItem = layoutInflater.inflate(R.layout.doctor_item, parent, false);
                break;
            case "choose":
                listItem = layoutInflater.inflate(R.layout.choose_doctor_item, parent, false);
                break;
            case "appointment_choose":
                listItem = layoutInflater.inflate(R.layout.my_appointment_item, parent, false);
                break;
        }
        return new ViewHolder(listItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctors doctors = doctorsList.get(position);
        holder.name.setText(String.format("Dr. %s", doctors.getName()));
        Glide.with(context).load(doctors.getPhoto()).placeholder(context.getDrawable(R.drawable.doctor_plus)).into(holder.photo);
        holder.designation.setText(doctors.getDesignation());
        holder.rating.setText(doctors.getRating());
        if (holder.consultationFees != null) {
            holder.consultationFees.setText(String.format("Consultation Fees\n\u20B9 %s", doctors.getConsultationFees()));
        }
        if (holder.fees != null) {
            holder.fees.setText(String.format("\u20B9 %s", doctors.getConsultationFees()));
        }
        switch (type) {
            case "choose":
                holder.parentLayout.setOnClickListener(v -> {
                    row_index = position;
                    notifyDataSetChanged();
                });

                if (row_index == position) {
                    holder.parentLayout.setBackground(context.getResources().getDrawable(R.drawable.text_view_round_selected));
                } else {
                    holder.parentLayout.setBackground(context.getResources().getDrawable(R.drawable.textview_round));
                }
                break;
            case "appointment_choose":
                holder.bookNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreferenceManager preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);
                        preferenceManager.setDoctorId(doctors.getDoctorId());
                        preferenceManager.setDoctorName(doctors.getName());
                        preferenceManager.setDoctorPhoto(doctors.getPhoto());
                        preferenceManager.setDesignation(doctors.getDesignation());
                        preferenceManager.setConsultationFees(doctors.getConsultationFees());
                        new ChooseDoctorForAppointment().getCurrentFragment((FragmentActivity) context);
                    }
                });
                break;
            case "dashboard":
                holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreferenceManager preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);
                        preferenceManager.setDoctorId(doctors.getDoctorId());
                        preferenceManager.setDoctorName(doctors.getName());
                        preferenceManager.setDoctorPhoto(doctors.getPhoto());
                        preferenceManager.setDesignation(doctors.getDesignation());
                        preferenceManager.setConsultationFees(doctors.getConsultationFees());
                        new Home().getCurrentFragment((FragmentActivity) context);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return doctorsList.size();
    }

    public List<String> getDoctor() {
        List<String> doctor = new ArrayList<>();
        if (row_index != -1) {
            doctor.add(doctorsList.get(row_index).getName());
            doctor.add(doctorsList.get(row_index).getDesignation());
            doctor.add(doctorsList.get(row_index).getRating());
        }
        return doctor;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, designation, rating, consultationFees, bookNow, fees;
        ConstraintLayout parentLayout;
        CircleImageView photo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            designation = itemView.findViewById(R.id.designation);
            rating = itemView.findViewById(R.id.rating);
            consultationFees = itemView.findViewById(R.id.consultationFees);
            fees = itemView.findViewById(R.id.fees);
            parentLayout = itemView.findViewById(R.id.parentLayout);
            bookNow = itemView.findViewById(R.id.bookNow);
            photo = itemView.findViewById(R.id.photo);
        }
    }
}
