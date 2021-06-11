package com.daphnistech.dtcskinclinic.firebase;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.daphnistech.dtcskinclinic.helper.Config;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class FirebasePushNotification extends FirebaseMessagingService {
    private static final String TAG = FirebasePushNotification.class.getSimpleName();

    private NotificationUtils notificationUtils;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        boolean isBackground = Boolean.parseBoolean(remoteMessage.getData().get("is_background"));
        String flag = remoteMessage.getData().get("flag");
        String data = remoteMessage.getData().get("data");


        if (flag == null)
            return;

        switch (Integer.parseInt(flag)) {
            case Config.PUSH_STATUS:
                // push notification belongs to a chat room
                updateStatus(data);
                break;
            case Config.PUSH_MESSAGE:
                // push notification is specific to user
                processUserMessage(isBackground, data);
                break;
            case Config.PUSH_APPOINTMENT:
                processAppointment(data);
                break;
        }
    }

    private void processAppointment(String data) {
        try {
            JSONObject datObj = new JSONObject(data);

            String title = datObj.getString("title");
            String message = datObj.getString("message");
            String type = datObj.getString("type");

            showNotificationAppointment(getApplicationContext(), title, message, type);

        } catch (JSONException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatus(String data) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            try {
                JSONObject datObj = new JSONObject(data);

                String id = datObj.getString("id");
                String type = datObj.getString("type");
                String status = datObj.getString("is_online");

                // app is in foreground, broadcast the push message
                Intent pushStatus = new Intent(Config.PUSH_STATUS_UPDATE);
                pushStatus.putExtra("id", id);
                pushStatus.putExtra("type", type);
                pushStatus.putExtra("status", status);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushStatus);
            } catch (JSONException e){
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    private void processUserMessage(boolean isBackground, String data) {
        if (!isBackground) {

            try {
                JSONObject datObj = new JSONObject(data);

                int patient_id = datObj.getInt("patient_id");
                int doctor_id = datObj.getInt("doctor_id");
                String name = datObj.getString("name");
                String message = datObj.getString("message");
                String sender = datObj.getString("sender");
                String imageUrl = datObj.getString("image");
                String message_type = datObj.getString("message_type");
                String timestamp = datObj.getString("timestamp");
                int appointment_id = datObj.getInt("appointment_id");
                String appointment_status = datObj.getString("appointment_status");

                // verifying whether the app is in background or foreground
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("message_type", message_type);
                    pushNotification.putExtra("message_body", message);
                    pushNotification.putExtra("image", imageUrl);
                    pushNotification.putExtra("timestamp", timestamp);
                    pushNotification.putExtra("appointment_id", appointment_id);
                    pushNotification.putExtra("appointment_status", appointment_status);
                    pushNotification.putExtra("patient_id", patient_id);
                    pushNotification.putExtra("doctor_id", doctor_id);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                    // play notification sound
                    /*NotificationUtils notificationUtils = new NotificationUtils();
                    notificationUtils.playNotificationSound();*/
                } else {

                    // check for push notification image attachment
                    if (message_type.equals("text")) {
                        showNotificationMessage(getApplicationContext(), timestamp, message, null, appointment_id, name, sender.split("_id")[1], appointment_status );
                    } else {
                        // push notification contains image
                        // show it with the image
                        showNotificationMessage(getApplicationContext(),  timestamp, message, imageUrl, appointment_id, name, sender.split("_id")[1], appointment_status);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
    }

    /**
     * Showing notification with text and image
     * */
    private void showNotificationMessage(Context context, String timestamp, String message, String imageUrl, int appointment_id, String name, String id, String appointment_status) {
        notificationUtils = new NotificationUtils(context);
        //notificationUtils.showNotificationMessage(title, message, imageUrl, appointment_id, name, id, appointment_status);
        notificationUtils.generateMessagingStyleNotification(timestamp, message, imageUrl, appointment_id, name, id, appointment_status);
    }

    private void showNotificationAppointment(Context context, String title, String message, String type) {
        notificationUtils = new NotificationUtils(context);
        notificationUtils.showNotificationAppointment(title, message, type);
    }
}
