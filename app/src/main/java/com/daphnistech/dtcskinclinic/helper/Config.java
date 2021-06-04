package com.daphnistech.dtcskinclinic.helper;

public class Config {

    // flag to identify whether to show single line
    // or multi line test push notification tray
    public static boolean appendNotificationMessages = true;

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";
    public static final String PUSH_STATUS_UPDATE = "pushStatus";

    // type of push messages
    public static final int PUSH_STATUS = 1;
    public static final int PUSH_MESSAGE = 2;
    public static final int PUSH_APPOINTMENT = 3;

    // id to handle the notification in the notification try
    public static final int APPOINTMENT_NOTIFICATION_ID = 100;
    public static final int MESSAGE_NOTIFICATION_ID = 101;
}