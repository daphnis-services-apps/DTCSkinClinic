package com.daphnistech.dtcskinclinic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.activity.ConversationActivity;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.model.Doctors;
import com.daphnistech.dtcskinclinic.model.Patients;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    String type;
    Context context;
    List<Doctors> doctorChatList;
    List<Patients> patientChatList;

    public ChatListAdapter(Context context, List<Doctors> chatList, List<Patients> patientChatList) {
        this.context = context;
        this.doctorChatList = chatList;
        this.patientChatList = patientChatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.chat_list_item, parent, false);
        return new ViewHolder(listItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int appointment_id;
        int id;
        boolean isOnline;
        int unreadCount;
        if (new PreferenceManager(context, Constant.USER_DETAILS).getLoginType().equals(Constant.DOCTOR)) {
            Patients patients = patientChatList.get(position);
            appointment_id = patients.getAppointmentId();
            holder.name.setText(patients.getName());
            holder.designation.setText(String.format("%s Years", patients.getAge()));
            isOnline = Boolean.parseBoolean(patients.getIsOnline());
            id = patients.getPatientId();
            unreadCount = patients.getUnreadCount();
        } else {
            Doctors doctors = doctorChatList.get(position);
            appointment_id = doctors.getAppointmentId();
            holder.name.setText(String.format("Dr. %s", doctors.getName()));
            holder.designation.setText(doctors.getDesignation());
            isOnline = Boolean.parseBoolean(doctors.isOnline());
            id = doctors.getDoctorId();
            unreadCount = doctors.getUnreadCount();
        }

        holder.status.setText(isOnline ? "ONLINE" : "OFFLINE");
        if (holder.status.getText().equals("ONLINE"))
            holder.status.setTextColor(Color.GREEN);
        else holder.status.setTextColor(Color.RED);

        if (unreadCount > 0) {
            holder.parentLayout.setBackground(context.getResources().getDrawable(R.drawable.text_view_round_selected));
            holder.count.setVisibility(View.VISIBLE);
            holder.count.setText(String.valueOf(unreadCount));
        }

        holder.parentLayout.setOnClickListener(v ->
                context.startActivity(
                        new Intent(context, ConversationActivity.class)
                                .putExtra("appointment_id", appointment_id)
                                .putExtra("name", holder.name.getText().toString())
                                .putExtra("receiver_id", id)
                                .putExtra("is_online", isOnline)
                                .putExtra("appointment_status","open")
                                .putExtra("unread_count", unreadCount)
                )
        );

    }

    @Override
    public int getItemCount() {
        if (new PreferenceManager(context, Constant.USER_DETAILS).getLoginType().equals(Constant.DOCTOR)) {
            return patientChatList.size();
        } else {
            return doctorChatList.size();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, designation, status, count;
        ConstraintLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            designation = itemView.findViewById(R.id.designation);
            status = itemView.findViewById(R.id.status);
            parentLayout = itemView.findViewById(R.id.parentLayout);
            count = itemView.findViewById(R.id.count);
        }
    }
}
