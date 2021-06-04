package com.daphnistech.dtcskinclinic.helper;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@SuppressLint("SimpleDateFormat")
public class DateHelper {
    public static String getCurrentTime(String type) {
        SimpleDateFormat sdf;
        if(type.equals("date"))
            sdf = new SimpleDateFormat("dd-MMM-yyyy");
        else sdf = new SimpleDateFormat("hh:mm aa");
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        return sdf.format(calendar.getTime());
    }

    public static String getDate(String timestamp, String type) {

        SimpleDateFormat sdf;
        if(type.equals("date"))
            sdf = new SimpleDateFormat("dd-MMM-yyyy");
        else sdf = new SimpleDateFormat("hh:mm aa");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        return sdf.format(calendar.getTime());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long getDateDifference(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(Long.parseLong(timestamp));

        LocalDate d1 = LocalDate.parse(sdf.format(calendar1.getTime()), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate d2 = LocalDate.parse(sdf.format(calendar.getTime()), DateTimeFormatter.ISO_LOCAL_DATE);
        return Duration.between(d1.atStartOfDay(), d2.atStartOfDay()).toDays();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long getDateDifference(String currentDate, String previousDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(currentDate));

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(Long.parseLong(previousDate));

        LocalDate d1 = LocalDate.parse(sdf.format(calendar1.getTime()), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate d2 = LocalDate.parse(sdf.format(calendar.getTime()), DateTimeFormatter.ISO_LOCAL_DATE);
        return Duration.between(d1.atStartOfDay(), d2.atStartOfDay()).toDays();
    }
}
