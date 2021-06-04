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
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.daphnistech.dtcskinclinic.activity.DoctorDashboard;

import java.util.concurrent.TimeUnit;

import static com.daphnistech.dtcskinclinic.helper.Config.APPOINTMENT_NOTIFICATION_ID;

/**
 * Asynchronously handles snooze and dismiss actions for reminder app (and active Notification).
 * Notification for for reminder app uses BigTextStyle.
 */
public class AppointmentIntentService extends IntentService {

    private static final String TAG = "BigTextService";

    public static final String ACTION_DISMISS =
            "com.daphnistech.dtcskinclinic.firebase.action.DISMISS";
    public static final String ACTION_VIEW_APPOINTMENT =
            "com.daphnistech.dtcskinclinic.firebase.action.VIEW_APPOINTMENT";

    private static final long SNOOZE_TIME = TimeUnit.SECONDS.toMillis(5);

    public AppointmentIntentService() {
        super("BigTextIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent(): " + intent);

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DISMISS.equals(action)) {
                handleActionDismiss(getApplicationContext());
            } else if (ACTION_VIEW_APPOINTMENT.equals(action)) {
                handleActionViewAppointment();
            }
        }
    }

    /**
     * Handles action Dismiss in the provided background thread.
     */
    public static void handleActionDismiss(Context context) {
        Log.d(TAG, "handleActionDismiss()");

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(APPOINTMENT_NOTIFICATION_ID);
    }

    /**
     * Handles action Snooze in the provided background thread.
     */
    private void handleActionViewAppointment() {
        Log.d(TAG, "handleActionSnooze()");

        // You could use NotificationManager.getActiveNotifications() if you are targeting SDK 23
        // and above, but we are targeting devices with lower SDK API numbers, so we saved the
        // builder globally and get the notification back to recreate it later.

        Intent intent = new Intent(getApplicationContext(),DoctorDashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("push",true);
        startActivity(intent);
    }
}
