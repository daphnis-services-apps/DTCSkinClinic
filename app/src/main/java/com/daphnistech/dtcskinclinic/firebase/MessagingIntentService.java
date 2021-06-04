/*
Copyright 2016 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.daphnistech.dtcskinclinic.firebase;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.MessagingStyle;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.activity.ConversationActivity;
import com.daphnistech.dtcskinclinic.helper.Config;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.MyApplication;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.daphnistech.dtcskinclinic.helper.Config.MESSAGE_NOTIFICATION_ID;

/**
 * Asynchronously handles updating messaging app posts (and active Notification) with replies from
 * user in a conversation. Notification for social app use MessagingStyle.
 */
public class MessagingIntentService extends IntentService {

    public static final String ACTION_REPLY =
            "com.daphnistech.dtcskinclinic.firebase.action.REPLY";
    public static final String EXTRA_REPLY =
            "com.daphnistech.dtcskinclinic.firebase.extra.REPLY";
    private static final String TAG = "MessagingIntentService";
    private static CharSequence Name;
    private static CharSequence Message;
    private static CharSequence timestamp;
    private static BroadcastReceiver mRegistrationBroadcastReceiver;
    private static int receiver_id;

    public MessagingIntentService() {
        super("MessagingIntentService");
    }

    public static synchronized void getInstance(Context context) {

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MessagingIntentService.ACTION_REPLY)) {
                    // new push message is received
                    //handlePushNotification(intent);
                    receiver_id = intent.getIntExtra("receiver_id",0);
                    /*timestamp = intent.getStringExtra("timestamp");
                    Name = intent.getStringExtra("name");*/
                }
            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(MessagingIntentService.ACTION_REPLY));
    }
    /**
     * Handles action for replying to messages from the notification.
     */
    private void handleActionReply(CharSequence replyCharSequence) {
        Log.d(TAG, "handleActionReply(): " + replyCharSequence);

        if (replyCharSequence != null) {

            // TODO: Asynchronously save your message to Database and servers.

            NotificationCompat.Builder notificationCompatBuilder =
                    GlobalNotificationBuilder.getNotificationCompatBuilderInstance();

            Notification notification = notificationCompatBuilder.build();
            MessagingStyle messagingStyle =
                    MessagingStyle.extractMessagingStyleFromNotification(
                            notification);

            messagingStyle.addMessage(replyCharSequence, System.currentTimeMillis(), new Person.Builder()
                    .setName(new PreferenceManager(getApplicationContext(), Constant.USER_DETAILS).getName())
                    .setKey(new PreferenceManager(getApplicationContext(), Constant.USER_DETAILS).getMobile())
                    .setUri("tel:" + new PreferenceManager(getApplicationContext(), Constant.USER_DETAILS).getMobile())
                    //.setIcon(IconCompat.createWithBitmap(new NotificationUtils(getApplicationContext()).getBitmapFromURL("https://dtcskinclinic.globalexpomart.com/v1/messages/724896-download-(2).jpg")))
                    .setIcon(IconCompat.createWithResource(getApplicationContext(), R.drawable.doctor_plus))
                    .build());

            // Updates the Notification
            notification = notificationCompatBuilder.setStyle(messagingStyle).build();

            // Pushes out the updated Notification
            NotificationManagerCompat notificationManagerCompat =
                    NotificationManagerCompat.from(getApplicationContext());
            notificationManagerCompat.notify(MESSAGE_NOTIFICATION_ID, notification);
            MyApplication.getInstance().getPrefManager().clearNotifications();
            MessagingIntentService.getInstance(getApplicationContext());
            Intent broadcastIntent = new Intent(MessagingIntentService.ACTION_REPLY)
                    .putExtra("receiver_id", receiver_id);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent(): " + intent);

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REPLY.equals(action)) {
                sendMessage("text", getMessage(intent),"", "");
            }
        }
    }

    private void sendMessage(String messageType, CharSequence messageBody, String imageUri, String path) {
        int sender_id,patient_id,doctor_id;
        String senderId,receiverId;
        sender_id = new PreferenceManager(getApplicationContext(),Constant.USER_DETAILS).getUserID();
        if (new PreferenceManager(getApplicationContext(),Constant.USER_DETAILS).getLoginType().equals(Constant.PATIENT)) {
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
        String timestamp = String.valueOf(System.currentTimeMillis());
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
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), new PreferenceManager(getApplicationContext(), Constant.USER_DETAILS).getName());
        RequestBody type = RequestBody.create(MediaType.parse("text/plain"), messageType);
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(messageBody));
        RequestBody time = RequestBody.create(MediaType.parse("text/plain"), timestamp);
        RequestBody count = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(0));

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
                                //recyclerView.smoothScrollToPosition(conversationList.size()-1);
                                handleActionReply(messageBody);
                            } else {
                                Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                        //Parsing the JSON

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");

                    }
                } else if (response.errorBody() != null) {
                    Toast.makeText(getApplicationContext(), response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(getApplicationContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles action for replying to messages from the notification.
     */
    /*private void handleActionReply(CharSequence replyCharSequence) {
        Log.d(TAG, "handleActionReply(): " + replyCharSequence);

        if (replyCharSequence != null) {

            // TODO: Asynchronously save your message to Database and servers.

            /*
             * You have two options for updating your notification (this class uses approach #2):
             *
             *  1. Use a new NotificationCompatBuilder to create the Notification. This approach
             *  requires you to get *ALL* the information that existed in the previous
             *  Notification (and updates) and pass it to the builder. This is the approach used in
             *  the MainActivity.
             *
             *  2. Use the original NotificationCompatBuilder to create the Notification. This
             *  approach requires you to store a reference to the original builder. The benefit is
             *  you only need the new/updated information. In our case, the reply from the user
             *  which we already have here.
             *
             *  IMPORTANT NOTE: You shouldn't save/modify the resulting Notification object using
             *  its member variables and/or legacy APIs. If you want to retain anything from update
             *  to update, retain the Builder as option 2 outlines.
             */

    // Retrieves NotificationCompat.Builder used to create initial Notification
            /*NotificationCompat.Builder notificationCompatBuilder =
                    GlobalNotificationBuilder.getNotificationCompatBuilderInstance();

            // Recreate builder from persistent state if app process is killed
            if (notificationCompatBuilder == null) {
                // Note: New builder set globally in the method
                notificationCompatBuilder = recreateBuilderWithMessagingStyle();
            }

            // Since we are adding to the MessagingStyle, we need to first retrieve the
            // current MessagingStyle from the Notification itself.
            Notification notification = notificationCompatBuilder.build();
            MessagingStyle messagingStyle =
                    NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
                            notification);

            // Add new message to the MessagingStyle. Set last parameter to null for responses
            // from user.
            messagingStyle.addMessage(replyCharSequence, System.currentTimeMillis(), (Person) null);

            // Updates the Notification
            notification = notificationCompatBuilder.setStyle(messagingStyle).build();

            // Pushes out the updated Notification
            NotificationManagerCompat notificationManagerCompat =
                    NotificationManagerCompat.from(getApplicationContext());
            notificationManagerCompat.notify(MESSAGE_NOTIFICATION_ID, notification);
            MyApplication.getInstance().getPrefManager().clearNotifications();
        }
    }*/

    /*
     * Extracts CharSequence created from the RemoteInput associated with the Notification.
     */
    private CharSequence getMessage(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_REPLY);
        }
        return null;
    }

    /*
     * This recreates the notification from the persistent state in case the app process was killed.
     * It is basically the same code for creating the Notification from MainActivity.
     */
    private NotificationCompat.Builder recreateBuilderWithMessagingStyle() {
        Context mContext = this;
        // Main steps for building a MESSAGING_STYLE notification:
        //      0. Get your data
        //      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
        //      2. Build the MESSAGING_STYLE
        //      3. Set up main Intent for notification
        //      4. Set up RemoteInput (users can input directly from notification)
        //      5. Build and issue the notification

        // 0. Get your data (everything unique per Notification)
        MockDatabase.MessagingStyleCommsAppData messagingStyleCommsAppData =
                MockDatabase.getMessagingStyleData(getApplicationContext());

        // 1. Create/Retrieve Notification Channel for O and beyond devices (26+).
        String notificationChannelId =
                MockDatabase.createNotificationChannel(this, messagingStyleCommsAppData);

        // 2. Build the NotificationCompat.Style (MESSAGING_STYLE).
        String contentTitle;

        NotificationCompat.MessagingStyle messagingStyle = null;

        // Adds all Messages.
        // Note: Messages include the text, timestamp, and sender.

        MyApplication.getInstance().getPrefManager().addNotification(String.valueOf(Message));

        // get the notifications from shared preferences
        String oldNotification = MyApplication.getInstance().getPrefManager().getNotifications();

        List<String> messages = Arrays.asList(oldNotification.split("\\|"));
        int number = messages.size();

        if (number > 1) {
            messagingStyle =
                    NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
                            GlobalNotificationBuilder.getNotificationCompatBuilderInstance().build());
            NotificationCompat.MessagingStyle.Message messagesA = new NotificationCompat.MessagingStyle.Message(
                    Message, Long.parseLong(String.valueOf(timestamp)), new Person.Builder().setName(Name).setKey("2233221122").setUri("tel:2233221122").setIcon(IconCompat.createWithResource(mContext, R.drawable.doctor_plus)).build());
            messagingStyle.addMessage(messagesA);
            contentTitle = number + " Messages From " + Name;
            messagingStyle.setConversationTitle(contentTitle);
        } else {
            messagingStyle =
                    new NotificationCompat.MessagingStyle(messagingStyleCommsAppData.getMe());
            NotificationCompat.MessagingStyle.Message messagesA = new NotificationCompat.MessagingStyle.Message(
                    Message, Long.parseLong(String.valueOf(timestamp)), new Person.Builder().setName(Name).setKey("2233221122").setUri("tel:2233221122").setIcon(IconCompat.createWithResource(mContext, R.drawable.doctor_plus)).build());
            messagingStyle.addMessage(messagesA);
            contentTitle = "1 Message From " + Name;
            messagingStyle.setConversationTitle("1 Message From " + Name);
        }

        messagingStyle.setGroupConversation(true);
        // 3. Set up main Intent for notification.
        Intent notifyIntent = new Intent(mContext, ConversationActivity.class);

        // When creating your Intent, you need to take into account the back state, i.e., what
        // happens after your Activity launches and the user presses the back button.

        // There are two options:
        //      1. Regular activity - You're starting an Activity that's part of the application's
        //      normal workflow.

        //      2. Special activity - The user only sees this Activity if it's started from a
        //      notification. In a sense, the Activity extends the notification by providing
        //      information that would be hard to display in the notification itself.

        // Even though this sample's MainActivity doesn't link to the Activity this Notification
        // launches directly, i.e., it isn't part of the normal workflow, a chat app generally
        // always links to individual conversations as part of the app flow, so we will follow
        // option 1.

        // For an example of option 2, check out the BIG_TEXT_STYLE example.

        // For more information, check out our dev article:
        // https://developer.android.com/training/notify-user/navigation.html

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(ConversationActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(notifyIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent mainPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        // 4. Set up RemoteInput, so users can input (keyboard and voice) from notification.

        // Note: For API <24 (M and below) we need to use an Activity, so the lock-screen present
        // the auth challenge. For API 24+ (N and above), we use a Service (could be a
        // BroadcastReceiver), so the user can input from Notification or lock-screen (they have
        // choice to allow) without leaving the notification.

        // Create the RemoteInput specifying this key.
        String replyLabel = "Reply";
        RemoteInput remoteInput = new RemoteInput.Builder(MessagingIntentService.EXTRA_REPLY)
                .setLabel(replyLabel)
                // Use machine learning to create responses based on previous messages.
                .setChoices(messagingStyleCommsAppData.getReplyChoicesBasedOnLastMessage())
                .build();

        // Pending intent =
        //      API <24 (M and below): activity so the lock-screen presents the auth challenge.
        //      API 24+ (N and above): this should be a Service or BroadcastReceiver.
        PendingIntent replyActionPendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(mContext, MessagingIntentService.class);
            intent.setAction(MessagingIntentService.ACTION_REPLY);
            replyActionPendingIntent = PendingIntent.getService(mContext, 0, intent, 0);
        } else {
            replyActionPendingIntent = mainPendingIntent;
        }

        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(
                        R.mipmap.ic_launcher,
                        replyLabel,
                        replyActionPendingIntent)
                        .addRemoteInput(remoteInput)
                        // Informs system we aren't bringing up our own custom UI for a reply
                        // action.
                        .setShowsUserInterface(false)
                        // Allows system to generate replies by context of conversation.
                        .setAllowGeneratedReplies(true)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                        .build();


        // 5. Build and issue the notification.

        // Because we want this to be a new notification (not updating current notification), we
        // create a new Builder. Later, we update this same notification, so we need to save this
        // Builder globally (as outlined earlier).
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            List<ShortcutInfoCompat> shortcutInfoCompatList = new ArrayList<>();

            ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(mContext, "id1")
                    .setLongLived(true)
                    .setShortLabel("Website")

                    .setIntent(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.mysite.example.com/")))
                    .build();

            shortcutInfoCompatList.add(shortcut);
            ShortcutManagerCompat.addDynamicShortcuts(mContext, shortcutInfoCompatList);

        }

        NotificationCompat.Builder notificationCompatBuilder =
                new NotificationCompat.Builder(mContext, notificationChannelId);

        GlobalNotificationBuilder.setNotificationCompatBuilderInstance(notificationCompatBuilder);

        notificationCompatBuilder
                // MESSAGING_STYLE sets title and content for API 16 and above devices.
                .setStyle(messagingStyle)
                // Title for API < 16 devices.
                .setContentTitle(contentTitle)
                // Content for API < 16 devices.
                .setContentText(Message)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.advertisment))
                .setContentIntent(mainPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setColor(ContextCompat.getColor(mContext, R.color.loginChooser))
                .setSubText(Integer.toString(number))
                .setShortcutId("id1")
                .addAction(replyAction)
                .setCategory(Notification.CATEGORY_MESSAGE)

                // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
                // 'importance' which is set in the NotificationChannel. The integers representing
                // 'priority' are different from 'importance', so make sure you don't mix them.
                .setPriority(messagingStyleCommsAppData.getPriority())

                // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
                // visibility is set in the NotificationChannel.
                .setVisibility(messagingStyleCommsAppData.getChannelLockscreenVisibility());

        return notificationCompatBuilder;
    }
}
