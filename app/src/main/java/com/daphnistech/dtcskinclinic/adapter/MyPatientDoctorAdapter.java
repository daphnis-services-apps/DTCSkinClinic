package com.daphnistech.dtcskinclinic.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.activity.ConversationActivity;
import com.daphnistech.dtcskinclinic.model.MyPatientDoctor;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyPatientDoctorAdapter extends RecyclerView.Adapter<MyPatientDoctorAdapter.ViewHolder> {
    Context context;
    List<MyPatientDoctor> myPatientDoctorList;

    public MyPatientDoctorAdapter(Context context, List<MyPatientDoctor> myPatientDoctorList) {
        this.context = context;
        this.myPatientDoctorList = myPatientDoctorList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.my_list_item, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        MyPatientDoctor myPatientDoctor = myPatientDoctorList.get(position);
        holder.name.setText(myPatientDoctor.getName());
        holder.title.setText(myPatientDoctor.getTitle());

        holder.parentLayout.setOnClickListener(v -> context.startActivity(
                new Intent(context, ConversationActivity.class)
                        .putExtra("appointment_id", myPatientDoctor.getAppointmentId())
                        .putExtra("name", myPatientDoctor.getName())
                        .putExtra("receiver_id", myPatientDoctor.getId())
                        .putExtra("is_online", false)
                        .putExtra("appointment_status", myPatientDoctor.getAppointmentStatus())
                        .putExtra("unread_count", 0)
        ));
    }

    @Override
    public int getItemCount() {
        return myPatientDoctorList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, title;
        ConstraintLayout parentLayout;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            title = itemView.findViewById(R.id.title);
            parentLayout = itemView.findViewById(R.id.parentLayout);
        }
    }
}
