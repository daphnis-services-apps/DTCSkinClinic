package com.daphnistech.dtcskinclinic.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.model.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    Context context;
    List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.transactions_list_item, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.transactionId.setText(transaction.getTransactionId());
        holder.appointmentId.setText(String.valueOf(transaction.getAppointmentId()));
        holder.transactionStatus.setText(transaction.getTransactionStatus());
        holder.amount.setText(String.format("\u20B9 %s", transaction.getTransactionAmount()));
        holder.time.setText(String.format("%s\n%s", transaction.getTransactionDate(), transaction.getTransactionTime()));
        if (transaction.getTransactionStatus().equalsIgnoreCase("success")) {
            holder.transactionStatus.setTextColor(Color.GREEN);
        } else {
            holder.transactionStatus.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView transactionId, appointmentId, time, amount, transactionStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionId = itemView.findViewById(R.id.transactionId);
            appointmentId = itemView.findViewById(R.id.appId);
            time = itemView.findViewById(R.id.time);
            amount = itemView.findViewById(R.id.transactionAmount);
            transactionStatus = itemView.findViewById(R.id.transactionStatus);
        }
    }
}
