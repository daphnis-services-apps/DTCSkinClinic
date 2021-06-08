package com.daphnistech.dtcskinclinic.firebase;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
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
import com.daphnistech.dtcskinclinic.activity.DoctorDashboard;
import com.daphnistech.dtcskinclinic.helper.Config;
import com.daphnistech.dtcskinclinic.helper.MyApplication;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.daphnistech.dtcskinclinic.helper.Config.APPOINTMENT_NOTIFICATION_ID;
import static com.daphnistech.dtcskinclinic.helper.Config.MESSAGE_NOTIFICATION_ID;

/**
 * Created by Ravi on 01/06/15.
 */
public class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getSimpleName();

    private Context mContext;
    private Notification.Builder notification;
    private NotificationManager notificationManager;
    private NotificationManagerCompat mNotificationManagerCompat;

    public NotificationUtils() {
    }

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
        mNotificationManagerCompat = NotificationManagerCompat.from(mContext);
    }

    /**
     * Method checks if the app is in background or not
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }

        return isInBackground;
    }

    // Clears notification tray messages
    public static void clearNotifications() {
        NotificationManager notificationManager = (NotificationManager) MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void showNotificationAppointment(String title, String message, String imageUrl) {

        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get your data
        //      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up main Intent for notification
        //      4. Create additional Actions for the Notification
        //      5. Build and issue the notification

        // 0. Get your data (everything unique per Notification).
        MockDatabase.BigTextStyleReminderAppData bigTextStyleReminderAppData =
                MockDatabase.getBigTextStyleData();

        // 1. Create/Retrieve Notification Channel for O and beyond devices (26+).
        String notificationChannelId =
                MockDatabase.createNotificationChannel(mContext, bigTextStyleReminderAppData);


        // 2. Build the BIG_TEXT_STYLE.
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                // Overrides ContentText in the big form of the template.
                .bigText(message)
                // Overrides ContentTitle in the big form of the template.
                .setBigContentTitle(title)
                // Summary line after the detail section in the big form of the template.
                // Note: To improve readability, don't overload the user with info. If Summary Text
                // doesn't add critical information, you should skip it.
                .setSummaryText("Appointments");


        // 3. Set up main Intent for notification.
        Intent notifyIntent = new Intent(mContext, DoctorDashboard.class);

        // When creating your Intent, you need to take into account the back state, i.e., what
        // happens after your Activity launches and the user presses the back button.

        // There are two options:
        //      1. Regular activity - You're starting an Activity that's part of the application's
        //      normal workflow.

        //      2. Special activity - The user only sees this Activity if it's started from a
        //      notification. In a sense, the Activity extends the notification by providing
        //      information that would be hard to display in the notification itself.

        // For the BIG_TEXT_STYLE notification, we will consider the activity launched by the main
        // Intent as a special activity, so we will follow option 2.

        // For an example of option 1, check either the MESSAGING_STYLE or BIG_PICTURE_STYLE
        // examples.

        // For more information, check out our dev article:
        // https://developer.android.com/training/notify-user/navigation.html

        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notifyIntent.putExtra("push", true);

        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        // 4. Create additional Actions (Intents) for the Notification.

        // In our case, we create two additional actions: a Snooze action and a Dismiss action.
        // Snooze Action.
        Intent viewAppointmentIntent = new Intent(mContext, AppointmentIntentService.class);
        viewAppointmentIntent.setAction(AppointmentIntentService.ACTION_VIEW_APPOINTMENT);

        PendingIntent viewAppointmentPendingIntent = PendingIntent.getService(mContext, 0, viewAppointmentIntent, 0);
        NotificationCompat.Action viewAppointmentAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.chat_item_icon_border,
                        "View Appointment",
                        viewAppointmentPendingIntent)
                        .build();


        // Dismiss Action.
        Intent dismissIntent = new Intent(mContext, AppointmentIntentService.class);
        dismissIntent.setAction(AppointmentIntentService.ACTION_DISMISS);

        PendingIntent dismissPendingIntent = PendingIntent.getService(mContext, 0, dismissIntent, 0);
        NotificationCompat.Action dismissAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.chat_item_icon_border,
                        "Dismiss",
                        dismissPendingIntent)
                        .build();


        // 5. Build and issue the notification.

        // Because we want this to be a new notification (not updating a previous notification), we
        // create a new Builder. Later, we use the same global builder to get back the notification
        // we built here for the snooze action, that is, canceling the notification and relaunching
        // it several seconds later.

        // Notification Channel Id is ignored for Android pre O (26).
        NotificationCompat.Builder notificationCompatBuilder =
                new NotificationCompat.Builder(
                        mContext, notificationChannelId);

        GlobalNotificationBuilder.setNotificationCompatBuilderInstance(notificationCompatBuilder);

        Notification notification = notificationCompatBuilder
                // BIG_TEXT_STYLE sets title and content for API 16 (4.1 and after).
                .setStyle(bigTextStyle)
                // Title for API <16 (4.0 and below) devices.
                .setContentTitle(title)
                // Content for API <24 (7.0 and below) devices.
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(
                        mContext.getResources(),
                        R.drawable.advertisment))
                .setContentIntent(notifyPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                // Set primary color (important for Wear 2.0 Notifications).
                .setColor(ContextCompat.getColor(mContext, R.color.loginChooser))

                // SIDE NOTE: Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
                // devices and all Wear devices. If you have more than one notification and
                // you prefer a different summary notification, set a group key and create a
                // summary notification via
                // .setGroupSummary(true)
                // .setGroup(GROUP_KEY_YOUR_NAME_HERE)

                .setCategory(Notification.CATEGORY_REMINDER)

                // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
                // 'importance' which is set in the NotificationChannel. The integers representing
                // 'priority' are different from 'importance', so make sure you don't mix them.
                .setPriority(bigTextStyleReminderAppData.getPriority())

                // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
                // visibility is set in the NotificationChannel.
                .setVisibility(bigTextStyleReminderAppData.getChannelLockscreenVisibility())

                // Adds additional actions specified above.
                //.addAction(viewAppointmentAction)
                .addAction(dismissAction)

                .build();

        mNotificationManagerCompat.notify(APPOINTMENT_NOTIFICATION_ID, notification);
    }

    public void generateMessagingStyleNotification(final String timestamp, final String Message, String imageUrl, int appointment_id, String Name, String id, String appointment_status) {

        Log.d(TAG, "generateMessagingStyleNotification()");

        // Main steps for building a MESSAGING_STYLE notification:
        //      0. Get your data
        //      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
        //      2. Build the MESSAGING_STYLE
        //      3. Set up main Intent for notification
        //      4. Set up RemoteInput (users can input directly from notification)
        //      5. Build and issue the notification

        // 0. Get your data (everything unique per Notification)
        MockDatabase.MessagingStyleCommsAppData messagingStyleCommsAppData =
                MockDatabase.getMessagingStyleData(mContext);

        // 1. Create/Retrieve Notification Channel for O and beyond devices (26+).
        String notificationChannelId =
                MockDatabase.createNotificationChannel(mContext, messagingStyleCommsAppData);

        // 2. Build the NotificationCompat.Style (MESSAGING_STYLE).
        String contentTitle;

        NotificationCompat.MessagingStyle messagingStyle = null;

        // Adds all Messages.
        // Note: Messages include the text, timestamp, and sender.

        MyApplication.getInstance().getPrefManager().addNotification(Message);

        // get the notifications from shared preferences
        String oldNotification = MyApplication.getInstance().getPrefManager().getNotifications();

        List<String> messages = Arrays.asList(oldNotification.split("\\|"));
        int number = messages.size();

        if (number > 1) {
            if (isNotificationVisible()) {
                messagingStyle =
                        NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
                                GlobalNotificationBuilder.getNotificationCompatBuilderInstance().build());
                NotificationCompat.MessagingStyle.Message messagesA = new NotificationCompat.MessagingStyle.Message(
                        Message, Long.parseLong(timestamp), new Person.Builder().setName(Name).setKey("2233221122").setUri("tel:2233221122").setIcon(IconCompat.createWithResource(mContext, R.drawable.doctor_plus)).build());
                messagingStyle.addMessage(messagesA);
                contentTitle = number + " Messages From " + Name;
                messagingStyle.setConversationTitle(contentTitle);
            } else {
                messagingStyle =
                        new NotificationCompat.MessagingStyle(messagingStyleCommsAppData.getMe());
                NotificationCompat.MessagingStyle.Message messagesA = new NotificationCompat.MessagingStyle.Message(
                        Message, Long.parseLong(timestamp), new Person.Builder().setName(Name).setKey("2233221122").setUri("tel:2233221122").setIcon(IconCompat.createWithResource(mContext, R.drawable.doctor_plus)).build());
                messagingStyle.addMessage(messagesA);
                messagingStyle.setConversationTitle("1 Message From " + Name);
            }
        } else {
            messagingStyle =
                    new NotificationCompat.MessagingStyle(messagingStyleCommsAppData.getMe());
            NotificationCompat.MessagingStyle.Message messagesA = new NotificationCompat.MessagingStyle.Message(
                    Message, Long.parseLong(timestamp), new Person.Builder().setName(Name).setKey("2233221122").setUri("tel:2233221122").setIcon(IconCompat.createWithResource(mContext, R.drawable.doctor_plus)).build());
            messagingStyle.addMessage(messagesA);
            messagingStyle.setConversationTitle("1 Message From " + Name);
        }

        messagingStyle.setGroupConversation(true);

        // 3. Set up main Intent for notification.
        Intent notifyIntent = new Intent(mContext, ConversationActivity.class);
        notifyIntent.putExtra("appointment_id", appointment_id);
        notifyIntent.putExtra("appointment_status", appointment_status);
        notifyIntent.putExtra("receiver_id", Integer.parseInt(id));
        notifyIntent.putExtra("name", Name);
        notifyIntent.putExtra("is_online", false);
        notifyIntent.putExtra("unread_count", 0);

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
                        MESSAGE_NOTIFICATION_ID,
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
            replyActionPendingIntent = PendingIntent.getService(mContext, MESSAGE_NOTIFICATION_ID, intent, 0);
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
                    .setShortLabel("Conversation")

                    .setIntent(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.mysite.example.com/")))
                    .build();

            shortcutInfoCompatList.add(shortcut);
            ShortcutManagerCompat.addDynamicShortcuts(mContext, shortcutInfoCompatList);

        }

        NotificationCompat.Builder notificationCompatBuilder =
                new NotificationCompat.Builder(mContext, notificationChannelId);

        GlobalNotificationBuilder.setNotificationCompatBuilderInstance(notificationCompatBuilder);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationCompatBuilder
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(Message)
                    .setAutoCancel(true)
                    .setStyle(messagingStyle)
                    .setContentIntent(mainPendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setColor(ContextCompat.getColor(mContext, R.color.loginChooser))
                    .setSubText(Integer.toString(number))
                    .setShortcutId("id1")
                    .addAction(replyAction)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setNumber(number);
        } else {
            notificationCompatBuilder
                    .setSmallIcon(R.drawable.doctor_plus)
                    .setContentText(Message)
                    .setAutoCancel(true)
                    .setNumber(number)
                    .setContentIntent(mainPendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setColor(ContextCompat.getColor(mContext, R.color.loginChooser))
                    .setSubText(Integer.toString(number))
                    .setShortcutId("id1")
                    .setSound(Uri.parse("android.resource://"
                            + mContext.getPackageName() + "/" + R.raw.notification))
                    .addAction(replyAction)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setStyle(messagingStyle)
                    .setPriority(Notification.PRIORITY_MAX);

        }

        if (imageUrl != null && imageUrl.length() > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {

            Bitmap bitmap = getBitmapFromURL(imageUrl);
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
            bigPictureStyle.setBigContentTitle(Name);
            bigPictureStyle.setSummaryText(Html.fromHtml(Message).toString());
            bigPictureStyle.bigPicture(bitmap);
            notificationCompatBuilder.setLargeIcon(bitmap);
            //playNotificationSound();
        }

        Notification notification = notificationCompatBuilder.build();
        mNotificationManagerCompat.notify(MESSAGE_NOTIFICATION_ID, notification);
        MessagingIntentService.getInstance(mContext);
        Intent broadcastIntent = new Intent(MessagingIntentService.ACTION_REPLY)
                .putExtra("receiver_id", Integer.parseInt(id));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);
    }

    public void showNotificationMessage(final String title, final String message, String imageUrl, int appointment_id, String name, String id, String appointment_status) {
        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;

        NotificationChannel notificationChannel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            final Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.getPackageName() + "/" + R.raw.notification);
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            notificationChannel = new NotificationChannel("1056", "Testing", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setSound(alarmSound, att);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        if (!TextUtils.isEmpty(imageUrl)) {

            if (imageUrl != null && imageUrl.length() > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {

                Bitmap bitmap = getBitmapFromURL(imageUrl);
                smallNotification(bitmap, title, message, appointment_id, name, id, appointment_status);
                //playNotificationSound();
            }
        } else {
            //showSmallNotification(mBuilder, icon, title, message, resultPendingIntent, alarmSound);
            smallNotification(null, title, message, appointment_id, name, id, appointment_status);
            //playNotificationSound();
        }
    }

    private void smallNotification(Bitmap bitmap, String title, String message, int appointment_id, String name, String id, String appointment_status) {
        int number = 0;
        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();

        if (Config.appendNotificationMessages) {
            // store the notification in shared pref first
            MyApplication.getInstance().getPrefManager().addNotification(message);

            // get the notifications from shared preferences
            String oldNotification = MyApplication.getInstance().getPrefManager().getNotifications();

            List<String> messages = Arrays.asList(oldNotification.split("\\|"));
            number = messages.size();
            for (int i = messages.size() - 1; i >= 0; i--) {
                inboxStyle.addLine(messages.get(i));
            }
        } else {
            inboxStyle.addLine(message);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(mContext, "1056")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setNumber(number)
                    .setStyle(inboxStyle);
        } else {
            notification = new Notification.Builder(mContext)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setNumber(number)
                    .setPriority(Notification.PRIORITY_MAX);

        }
        if (bitmap != null) {
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
            bigPictureStyle.setBigContentTitle(title);
            bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
            bigPictureStyle.bigPicture(bitmap);
            notification.setLargeIcon(bitmap);
        }

        Intent returnIntent = new Intent(mContext, ConversationActivity.class);
        returnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        returnIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        returnIntent.putExtra("appointment_id", appointment_id);
        returnIntent.putExtra("appointment_status", appointment_status);
        returnIntent.putExtra("receiver_id", Integer.parseInt(id));
        returnIntent.putExtra("name", name);
        returnIntent.putExtra("is_online", false);
        returnIntent.putExtra("unread_count", 0);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntent(returnIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);
        notificationManager.notify(0, notification.build());
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            if (mContext != null) {
                Looper.prepare();
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

    // Playing notification sound
    public void playNotificationSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + MyApplication.getInstance().getApplicationContext().getPackageName() + "/" + R.raw.notification);
            Ringtone r = RingtoneManager.getRingtone(MyApplication.getInstance().getApplicationContext(), alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNotificationVisible() {
        Intent notificationIntent = new Intent(mContext, ConversationActivity.class);
        PendingIntent test = PendingIntent.getActivity(mContext, MESSAGE_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_NO_CREATE);
        return test != null;
    }


}

