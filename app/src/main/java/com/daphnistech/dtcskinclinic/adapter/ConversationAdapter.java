package com.daphnistech.dtcskinclinic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.activity.ConversationActivity;
import com.daphnistech.dtcskinclinic.activity.FullScreenImageActivity;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.model.Conversation;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.daphnistech.dtcskinclinic.helper.DateHelper.getCurrentTime;
import static com.daphnistech.dtcskinclinic.helper.DateHelper.getDate;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    Context context;
    List<Conversation> conversationList;
    private int position;
    private final String currentDate = getCurrentTime("date");
    private String previousDate;
    private int dateCounter;

    public ConversationAdapter(Context context, List<Conversation> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = null;
        if (viewType == Constant.SENDER) {
            listItem = layoutInflater.inflate(R.layout.conversation_item_sender, parent, false);
        }
        if (viewType == Constant.RECEIVER) {
            listItem = layoutInflater.inflate(R.layout.conversation_item_receiver, parent, false);
        }
        assert listItem != null;
        return new ViewHolder(listItem);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        if (holder.textMessage != null && holder.cardView != null && holder.imageView != null) {
            if (conversation.getMessageType().equals("text")) {
                holder.textMessage.setVisibility(View.VISIBLE);
                holder.cardView.setVisibility(View.GONE);
                holder.textMessage.setText(conversation.getMessageBody());
            } else {
                holder.textMessage.setVisibility(View.GONE);
                holder.cardView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(conversation.getImage())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.picProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(holder.imageView);

                holder.imageView.setOnClickListener(v -> {
                    BitmapDrawable drawable = (BitmapDrawable) holder.imageView.getDrawable();
                    if (drawable != null) {
                        Intent intent = new Intent(context, FullScreenImageActivity.class);
                        Bitmap bitmap = drawable.getBitmap();
                        ByteArrayOutputStream bs = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
                        intent.putExtra("byteArray", bs.toByteArray());
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Please wait, Image is downloading", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            holder.timeStamp.setText(getDate(conversation.getTimeStamp(), "time"));
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && position!=0) {
                previousDate = getDate(conversationList.get(position-1).getTimeStamp(),"date");
                currentDate = getDate(conversation.getTimeStamp(),"date");
                if(getDateDifference(conversation.getTimeStamp(), conversationList.get(position-1).getTimeStamp())!=0){
                    dateCounter++;
                    Toast.makeText(context, String.valueOf(dateCounter), Toast.LENGTH_SHORT).show();
                    holder.dateText.setVisibility(View.VISIBLE);
                    holder.date.setText(getDate(conversation.getTimeStamp(),"date"));
                }
            }*/
            if (!ConversationActivity.send && this.position == position)
                holder.sending.setVisibility(View.VISIBLE);
            else holder.sending.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        PreferenceManager preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);
        Conversation conversation = conversationList.get(position);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && position != 0) {
            previousDate = getDate(conversationList.get(position - 1).getTimeStamp(), "date");
            currentDate = getDate(conversation.getTimeStamp(), "date");
            if (getDateDifference(conversation.getTimeStamp(), conversationList.get(position - 1).getTimeStamp()) != 0) {
                dateCounter++;
            }
        }*/
        String tempId;
        String userId = conversation.getSenderId();
        if (preferenceManager.getLoginType().equals(Constant.PATIENT))
            tempId = Constant.PATIENT_ID + preferenceManager.getUserID();
        else tempId = Constant.DOCTOR_ID + preferenceManager.getUserID();
        return userId.equalsIgnoreCase(tempId) ? Constant.SENDER : Constant.RECEIVER;
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public void setLastPosition(int position) {
        this.position = position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, timeStamp, date;
        ImageView imageView, sending;
        CardView cardView;
        ProgressBar picProgressBar;
        ConstraintLayout dateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            imageView = itemView.findViewById(R.id.imageMessage);
            cardView = itemView.findViewById(R.id.imageCard);
            sending = itemView.findViewById(R.id.sending);
            date = itemView.findViewById(R.id.date);
            dateText = itemView.findViewById(R.id.dateText);
            picProgressBar = itemView.findViewById(R.id.picProgressBar);
        }
    }
}
