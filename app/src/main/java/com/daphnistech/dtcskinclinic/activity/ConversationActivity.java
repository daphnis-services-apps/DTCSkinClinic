package com.daphnistech.dtcskinclinic.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.adapter.ConversationAdapter;
import com.daphnistech.dtcskinclinic.firebase.NotificationUtils;
import com.daphnistech.dtcskinclinic.helper.ChatOptions;
import com.daphnistech.dtcskinclinic.helper.Config;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.MyApplication;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.daphnistech.dtcskinclinic.model.Conversation;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.daphnistech.dtcskinclinic.helper.Config.MESSAGE_NOTIFICATION_ID;
import static com.daphnistech.dtcskinclinic.helper.DateHelper.getDateDifference;

@SuppressWarnings("deprecation")
public class ConversationActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {
    public static boolean send = true;
    ImageView sendButton, imageButton, online, options, back;
    EditText message;
    Context context;
    List<Conversation> conversationList;
    RecyclerView recyclerView;
    ConversationAdapter conversationAdapter;
    TextView noMessage, name, date, markClosedOrOpen;
    LottieAnimationView animationView;
    CardView dateCard;
    int receiver_id, sender_id;
    int patient_id, doctor_id;
    String senderId, receiverId;
    LinearLayout messageLayout, markClosedText, patientMarkClosedText;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private PreferenceManager preferenceManager;
    private int unread_count;
    private String appointment_status;
    private LinearLayoutManager layoutManager;
    private CircleImageView profile;
    private boolean imageSelect = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }
        initViews();
        // Cancel Notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(MESSAGE_NOTIFICATION_ID);
        MyApplication.getInstance().getPrefManager().clearNotifications();
        preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);
        if (preferenceManager.getLoginType().equals(Constant.PATIENT))
            options.setVisibility(View.GONE);


        message.addTextChangedListener(this);
        imageButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                if (conversationList.size() > 1) {
                    recyclerView.postDelayed(() -> recyclerView.smoothScrollToPosition(conversationList.size() - 1), 100);
                }
            }
        });

        options.setOnClickListener(v -> ChatOptions.showProgressBar(context, true, appointment_status.equalsIgnoreCase("open")));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                new Handler().postDelayed(() -> dateCard.setVisibility(View.GONE), 600);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                Conversation conversation = conversationList.get(firstVisiblePosition);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (getDateDifference(conversation.getTimeStamp()) == 0) {
                        dateCard.setVisibility(View.VISIBLE);
                        date.setText(R.string.today);
                    } else if (getDateDifference(conversation.getTimeStamp()) == 1) {
                        dateCard.setVisibility(View.VISIBLE);
                        date.setText(R.string.yesterday);
                    } else {
                        dateCard.setVisibility(View.VISIBLE);
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(conversation.getTimeStamp()));
                        date.setText(sdf.format(calendar.getTime()));
                    }
                }
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push message is received
                    handlePushNotification(intent);
                } else {
                    if (Integer.parseInt(intent.getStringExtra("id")) == receiver_id)
                        setOnlineStatus(Boolean.parseBoolean(intent.getStringExtra("status")));
                }
            }
        };
    }

    /**
     * Handling new push message, will add the message to
     * recycler view and scroll it to bottom
     */
    private void handlePushNotification(Intent intent) {
        String messageType = intent.getStringExtra("message_type");
        String messageBody = intent.getStringExtra("message_body");
        String imageUri = intent.getStringExtra("image");
        String timestamp = intent.getStringExtra("timestamp");

        if (message != null) {
            conversationList.add(new Conversation(receiverId, messageType, messageBody, imageUri, timestamp));
            conversationAdapter.notifyDataSetChanged();
            if (conversationAdapter.getItemCount() > 1) {
                recyclerView.smoothScrollToPosition(conversationList.size() - 1);
            }
        }
    }

    private void putStatus(boolean status) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call = api.putStatus(preferenceManager.getUserID(), preferenceManager.getLoginType(), String.valueOf(status));

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (jsonObject.getBoolean("error")) {
                                Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                        //Parsing the JSON

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(context, "Returned empty response", Toast.LENGTH_SHORT).show();
                    }
                } else if (response.errorBody() != null) {
                    Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getConversations() {
        conversationList = new ArrayList<>();
        CustomProgressBar.showProgressBar(context, false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call = api.getConversation(patient_id, doctor_id, receiverId);

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
                                    JSONObject conversations = new JSONObject(jsonArray.getString(i));
                                    conversationList.add(new Conversation(conversations.getString("sender"), conversations.getString("message_type"), conversations.getString("message_body"), conversations.getString("image"), conversations.getString("timestamp")));
                                }
                                conversationAdapter.notifyDataSetChanged();
                                CustomProgressBar.hideProgressBar();
                                if (conversationList.size() == 0) {
                                    animationView.setVisibility(View.VISIBLE);
                                    noMessage.setVisibility(View.VISIBLE);
                                }
                                if (appointment_status.equalsIgnoreCase("closed")) {
                                    messageLayout.setVisibility(View.GONE);
                                    if (preferenceManager.getLoginType().equals(Constant.PATIENT))
                                        patientMarkClosedText.setVisibility(View.VISIBLE);
                                    else
                                        markClosedText.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                CustomProgressBar.hideProgressBar();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            CustomProgressBar.hideProgressBar();
                        }
                        //Parsing the JSON

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(context, "Returned empty response", Toast.LENGTH_SHORT).show();
                        CustomProgressBar.hideProgressBar();
                    }
                } else if (response.errorBody() != null) {
                    Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                    CustomProgressBar.hideProgressBar();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                CustomProgressBar.hideProgressBar();
            }
        });
    }

    private void initViews() {
        context = ConversationActivity.this;
        PreferenceManager preferenceManager = new PreferenceManager(context, Constant.USER_DETAILS);
        sendButton = findViewById(R.id.sendButton);
        message = findViewById(R.id.message);
        imageButton = findViewById(R.id.imageButton);
        recyclerView = findViewById(R.id.conversationRecyclerView);
        noMessage = findViewById(R.id.noMessage);
        profile = findViewById(R.id.profile);
        name = findViewById(R.id.name);
        back = findViewById(R.id.back);
        online = findViewById(R.id.online);
        animationView = findViewById(R.id.animationView);
        date = findViewById(R.id.date);
        dateCard = findViewById(R.id.dateCard);
        options = findViewById(R.id.option);
        messageLayout = findViewById(R.id.messageLayout);
        markClosedText = findViewById(R.id.markClosedText);
        patientMarkClosedText = findViewById(R.id.patientMarkClosedText);
        markClosedOrOpen = findViewById(R.id.markClosed);

        Glide.with(context).load(getIntent().getStringExtra("profile")).placeholder(getDrawable(R.drawable.doctor_plus)).into(profile);
        name.setText(getIntent().getStringExtra("name"));
        receiver_id = getIntent().getIntExtra("receiver_id", 0);
        unread_count = getIntent().getIntExtra("unread_count", 0);
        appointment_status = getIntent().getStringExtra("appointment_status");
        sender_id = preferenceManager.getUserID();
        if (preferenceManager.getLoginType().equals(Constant.PATIENT)) {
            patient_id = sender_id;
            doctor_id = receiver_id;
            senderId = Constant.PATIENT_ID + sender_id;
            receiverId = Constant.DOCTOR_ID + doctor_id;
        } else {
            doctor_id = sender_id;
            patient_id = receiver_id;
            senderId = Constant.DOCTOR_ID + sender_id;
            receiverId = Constant.PATIENT_ID + patient_id;
        }
        setOnlineStatus(getIntent().getBooleanExtra("is_online", false));
    }

    private void setOnlineStatus(boolean is_online) {
        if (is_online)
            online.setVisibility(View.VISIBLE);
        else online.setVisibility(View.GONE);
    }

    private void toggle(boolean show) {
        ViewGroup parent = findViewById(R.id.parentLayout);

        Transition transition = new Slide(Gravity.END);
        transition.setDuration(300);
        transition.addTarget(R.id.sendButton);

        TransitionManager.beginDelayedTransition(parent, transition);
        sendButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        toggle(s.toString().length() > 0);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageButton) {
            imageSelect = true;
            ImagePicker.Companion
                    .with((Activity) context)
                    .cropSquare()
                    .compress(1024)
                    .start();
        } else if (v.getId() == R.id.sendButton) {
            sendMessage("text", message.getText().toString(), "", "");
        }
    }

    public void chatOptions(View v) {
        if (v.getId() == R.id.viewDetails) {
            Intent intent = new Intent(context, ViewPatientDetails.class);
            intent.putExtra("id", patient_id);
            startActivity(intent);
            ChatOptions.hideProgressBar();
        } else if (v.getId() == R.id.markClosed) {
            markClosedOrOpen(appointment_status.equalsIgnoreCase("closed") ? "CLOSED" : "OPEN");
        }
    }

    private void markClosedOrOpen(String type) {
        CustomProgressBar.showProgressBar(context, false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call;
        if (type.equalsIgnoreCase("OPEN"))
            call = api.markAppointmentClosed(getIntent().getIntExtra("appointment_id", 0));
        else call = api.markAppointmentOpen(getIntent().getIntExtra("appointment_id", 0));

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (Boolean.parseBoolean(response.body())) {
                            if (type.equalsIgnoreCase("closed")) {
                                Toast.makeText(context, "Appointment Opened Successfully", Toast.LENGTH_SHORT).show();
                                appointment_status = "OPEN";
                                messageLayout.setVisibility(View.VISIBLE);
                                if (preferenceManager.getLoginType().equals(Constant.PATIENT))
                                    patientMarkClosedText.setVisibility(View.GONE);
                                else
                                    markClosedText.setVisibility(View.GONE);
                                ChatOptions.hideProgressBar();
                            } else {
                                ChatOptions.hideProgressBar();
                                Toast.makeText(context, "Appointment Closed Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else
                            Toast.makeText(context, "Error Occurred. Please Try Again Later", Toast.LENGTH_SHORT).show();
                        CustomProgressBar.hideProgressBar();
                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(context, "Returned empty response", Toast.LENGTH_SHORT).show();
                        CustomProgressBar.hideProgressBar();
                    }
                } else if (response.errorBody() != null) {
                    Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                    CustomProgressBar.hideProgressBar();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                CustomProgressBar.hideProgressBar();
            }
        });
    }

    private void sendMessage(String messageType, String messageBody, String imageUri, String path) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        send = false;
        conversationList.add(new Conversation(senderId, messageType, messageBody, imageUri, timestamp));
        conversationAdapter.setLastPosition(conversationList.size() - 1);
        conversationAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(conversationList.size() - 1);
        message.setText("");
        animationView.setVisibility(View.GONE);
        noMessage.setVisibility(View.GONE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        MultipartBody.Part image;
        if (messageType.equalsIgnoreCase("image")) {
            File file = new File(path);
            // Create a request body with file and image media type
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
            // Create MultipartBody.Part using file request-body,file name and part name
            image = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);
        } else {
            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), "");
            image = MultipartBody.Part.createFormData("image", "", requestBody);
        }

        RequestBody patient = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(patient_id));
        RequestBody doctor = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(doctor_id));
        RequestBody temp_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(senderId));
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), new PreferenceManager(context, Constant.USER_DETAILS).getName());
        RequestBody type = RequestBody.create(MediaType.parse("text/plain"), messageType);
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), messageBody);
        RequestBody time = RequestBody.create(MediaType.parse("text/plain"), timestamp);
        RequestBody count = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(unread_count));

        Call<String> call = api.sendMessage(
                patient,
                doctor,
                temp_id,
                name,
                type,
                body,
                image,
                time,
                count);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (!jsonObject.getBoolean("error")) {
                                send = true;
                                conversationAdapter.notifyDataSetChanged();
                                //recyclerView.smoothScrollToPosition(conversationList.size()-1);
                            } else {
                                Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                        //Parsing the JSON

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");

                    }
                } else if (response.errorBody() != null) {
                    Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null)
            sendMessage("image", "N/A", data.getData().toString(), data.getData().getPath());
    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        if (NotificationUtils.isAppIsInBackground(context))
            putStatus(Constant.OFFLINE);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registering the receiver for new notification
        IntentFilter filter = new IntentFilter(Config.PUSH_NOTIFICATION);
        filter.addAction(Config.PUSH_STATUS_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, filter);
        if (!NotificationUtils.isAppIsInBackground(context))
            putStatus(Constant.ONLINE);

        NotificationUtils.clearNotifications();

        if (!imageSelect) {
            getConversations();
            conversationAdapter = new ConversationAdapter(context, conversationList);
            layoutManager = new LinearLayoutManager(context);
            layoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(conversationAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (preferenceManager.getLoginType().equals(Constant.PATIENT)) {
            startActivity(new Intent(context, PatientDashboard.class));
        } else {
            startActivity(new Intent(context, DoctorDashboard.class));
        }
        finish();
    }
}
