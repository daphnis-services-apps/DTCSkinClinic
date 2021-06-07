package com.daphnistech.dtcskinclinic.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.model.Appointments;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    Context context;
    List<Appointments> appointmentsList;

    public AppointmentAdapter(Context context, List<Appointments> appointmentsList) {
        this.context = context;
        this.appointmentsList = appointmentsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.my_appointment_item1, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointments appointments = appointmentsList.get(position);
        if (new PreferenceManager(context, Constant.USER_DETAILS).getLoginType().equals(Constant.PATIENT)) {
            holder.name.setText(String.format("Dr. %s", appointments.getName()));
            holder.designation.setText(appointments.getDesignation());
        } else {
            holder.name.setText(appointments.getName());
            holder.designation.setText(String.format("%s Years", appointments.getDesignation()));
        }
        Glide.with(context).load(appointments.getPhoto()).placeholder(context.getDrawable(R.drawable.doctor_plus)).into(holder.photo);
        holder.fees.setText(String.format("\u20B9 %s", appointments.getTransactionAmount()));
        holder.mode.setText(String.valueOf(appointments.getAppointmentId()) );
        holder.status.setText(appointments.getAppointmentStatus());
        if(appointments.getAppointmentStatus().equalsIgnoreCase("closed")){
            holder.status.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return appointmentsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, designation, fees, mode, status;
        ConstraintLayout parentLayout;
        CircleImageView photo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            designation = itemView.findViewById(R.id.designation);
            parentLayout = itemView.findViewById(R.id.parentLayout);
            fees = itemView.findViewById(R.id.fees);
            mode = itemView.findViewById(R.id.mode);
            status = itemView.findViewById(R.id.status);
            photo = itemView.findViewById(R.id.photo);
        }
    }
}
