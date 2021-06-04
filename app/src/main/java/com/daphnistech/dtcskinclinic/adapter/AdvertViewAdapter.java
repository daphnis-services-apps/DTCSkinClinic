package com.daphnistech.dtcskinclinic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.model.Advertisement;

import java.util.List;

public class AdvertViewAdapter extends RecyclerView.Adapter<AdvertViewAdapter.ViewHolder> {
    Context context;
    List<Advertisement> advertisementList;

    public AdvertViewAdapter(Context context, List<Advertisement> advertisementList) {
        this.context = context;
        this.advertisementList = advertisementList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.advertisement, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Advertisement advertisement = advertisementList.get(position);
        holder.imageView.setImageDrawable(advertisement.getImageView());
    }

    @Override
    public int getItemCount() {
        return advertisementList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.advertImage);
        }
    }
}
