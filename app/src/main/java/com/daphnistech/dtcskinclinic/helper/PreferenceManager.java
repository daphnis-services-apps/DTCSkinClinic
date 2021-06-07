package com.daphnistech.dtcskinclinic.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.messaging.FirebaseMessaging;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public PreferenceManager(Context context, String preferenceName) {
        sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static void clearAll(Context context) {
        context.getSharedPreferences(Constant.USER_DETAILS, MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(Constant.DISEASES, MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(Constant.SKIN_DISEASE, MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(Constant.COSMETOLOGY, MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(Constant.HAIR_PROBLEM, MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(Constant.SEX_DISEASE, MODE_PRIVATE).edit().clear().apply();
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constant.DOCTOR);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constant.PATIENT);
        FirebaseMessaging.getInstance().deleteToken();
    }

    public String getLoginType() {
        return sharedPreferences.getString(Constant.LOGIN_TYPE, "");
    }

    public void setLoginType(String loginType) {
        editor.putString(Constant.LOGIN_TYPE, loginType).apply();
    }

    public int getUserID() {
        return sharedPreferences.getInt(Constant.USER_ID, 0);
    }

    public void setUserID(int user_id) {
        editor.putInt(Constant.USER_ID, user_id).apply();
    }

    public String getMobile() {
        return sharedPreferences.getString(Constant.MOBILE, "");
    }

    public void setMobile(String mobile) {
        editor.putString(Constant.MOBILE, mobile).apply();
    }

    public int getSteps() {
        return sharedPreferences.getInt(Constant.STEPS, 0);
    }

    public void setSteps(int steps) {
        editor.putInt(Constant.STEPS, steps).apply();
    }

    public String getPic1() {
        return sharedPreferences.getString(Constant.PIC1, "");
    }

    public void setPic1(String pic) {
        editor.putString(Constant.PIC1, pic).apply();
    }

    public String getPic2() {
        return sharedPreferences.getString(Constant.PIC2, "");
    }

    public void setPic2(String pic) {
        editor.putString(Constant.PIC2, pic).apply();
    }

    public String getPic3() {
        return sharedPreferences.getString(Constant.PIC3, "");
    }

    public void setPic3(String pic) {
        editor.putString(Constant.PIC3, pic).apply();
    }

    public String getPDF() {
        return sharedPreferences.getString(Constant.PDF, "");
    }

    public void setPDF(String path) {
        editor.putString(Constant.PDF, path).apply();
    }

    public String getName() {
        return sharedPreferences.getString(Constant.NAME, "");
    }

    public void setName(String name) {
        editor.putString(Constant.NAME, name).apply();
    }

    public String getAge() {
        return sharedPreferences.getString(Constant.AGE, "");
    }

    public void setAge(String age) {
        editor.putString(Constant.AGE, age).apply();
    }

    public String getGender() {
        return sharedPreferences.getString(Constant.GENDER, "");
    }

    public void setGender(String gender) {
        editor.putString(Constant.GENDER, gender).apply();
    }

    public String getAddress() {
        return sharedPreferences.getString(Constant.ADDRESS, "");
    }

    public void setAddress(String address) {
        editor.putString(Constant.ADDRESS, address).apply();
    }

    public String getState() {
        return sharedPreferences.getString(Constant.STATE, "");
    }

    public void setState(String state) {
        editor.putString(Constant.STATE, state).apply();
    }

    public String getPIN() {
        return sharedPreferences.getString(Constant.PIN, "");
    }

    public void setPIN(String pin) {
        editor.putString(Constant.PIN, pin).apply();
    }

    public boolean isDiabetes() {
        return sharedPreferences.getBoolean(Constant.IS_DIABETES, false);
    }

    public void setDiabetes(boolean check) {
        editor.putBoolean(Constant.IS_DIABETES, check).apply();
    }

    public boolean isHyperTension() {
        return sharedPreferences.getBoolean(Constant.IS_HYPERTENSION, false);
    }

    public void setHyperTension(boolean check) {
        editor.putBoolean(Constant.IS_HYPERTENSION, check).apply();
    }

    public boolean isThyroidProblem() {
        return sharedPreferences.getBoolean(Constant.IS_THYROID_PROBLEM, false);
    }

    public void setThyroidProblem(boolean check) {
        editor.putBoolean(Constant.IS_THYROID_PROBLEM, check).apply();
    }

    public boolean isDrugAllergy() {
        return sharedPreferences.getBoolean(Constant.IS_DRUG_ALLERGY, false);
    }

    public void setDrugAllergy(boolean check) {
        editor.putBoolean(Constant.IS_DRUG_ALLERGY, check).apply();
    }

    public boolean isSkinDisease() {
        return sharedPreferences.getBoolean(Constant.IS_SKIN_DISEASE, false);
    }

    public void setSkinDisease(boolean check) {
        editor.putBoolean(Constant.IS_SKIN_DISEASE, check).apply();
    }

    public boolean isCosmetology() {
        return sharedPreferences.getBoolean(Constant.IS_COSMETOLOGY, false);
    }

    public void setCosmetology(boolean check) {
        editor.putBoolean(Constant.IS_COSMETOLOGY, check).apply();
    }

    public boolean isHairProblem() {
        return sharedPreferences.getBoolean(Constant.IS_HAIR_PROBLEM, false);
    }

    public void setHairProblem(boolean check) {
        editor.putBoolean(Constant.IS_HAIR_PROBLEM, check).apply();
    }

    public boolean isSexDisease() {
        return sharedPreferences.getBoolean(Constant.IS_SEX_PROBLEM, false);
    }

    public void setSexDisease(boolean check) {
        editor.putBoolean(Constant.IS_SEX_PROBLEM, check).apply();
    }

    public String getOldAge() {
        return sharedPreferences.getString(Constant.OLD_AGE, "");
    }

    public void setOldAge(String oldAge) {
        editor.putString(Constant.OLD_AGE, oldAge).apply();
    }

    public String getComments() {
        return sharedPreferences.getString(Constant.COMMENTS, "");
    }

    public void setComments(String comments) {
        editor.putString(Constant.COMMENTS, comments).apply();
    }

    public String getDiseaseType() {
        return sharedPreferences.getString(Constant.DISEASE_TYPE, "");
    }

    public void setDiseaseType(String diseaseType) {
        editor.putString(Constant.DISEASE_TYPE, diseaseType).apply();
    }

    public String getDoctorName() {
        return sharedPreferences.getString(Constant.DOCTOR_NAME, "");
    }

    public void setDoctorName(String doctorName) {
        editor.putString(Constant.DOCTOR_NAME, doctorName).apply();
    }

    public String getDesignation() {
        return sharedPreferences.getString(Constant.DESIGNATION, "");
    }

    public void setDesignation(String designation) {
        editor.putString(Constant.DESIGNATION, designation).apply();
    }

    public String getRating() {
        return sharedPreferences.getString(Constant.RATING, "");
    }

    public void setRating(String rating) {
        editor.putString(Constant.RATING, rating).apply();
    }

    public String getConsultationFees() {
        return sharedPreferences.getString(Constant.CONSULTATION_FEES, "");
    }

    public void setConsultationFees(String consultationFees) {
        editor.putString(Constant.CONSULTATION_FEES, consultationFees).apply();
    }

    public String getDrugRemarks() {
        return sharedPreferences.getString(Constant.DRUG_REMARKS, "");
    }

    public void setDrugRemarks(String drugRemarks) {
        editor.putString(Constant.DRUG_REMARKS, drugRemarks).apply();
    }

    public String getPic1Path() {
        return sharedPreferences.getString(Constant.PIC1_PATH, "");
    }

    public void setPic1Path(String path) {
        editor.putString(Constant.PIC1_PATH, path).apply();
    }

    public String getPic2Path() {
        return sharedPreferences.getString(Constant.PIC2_PATH, "");
    }

    public void setPic2Path(String path) {
        editor.putString(Constant.PIC2_PATH, path).apply();
    }

    public String getPic3Path() {
        return sharedPreferences.getString(Constant.PIC3_PATH, "");
    }

    public void setPic3Path(String path) {
        editor.putString(Constant.PIC3_PATH, path).apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constant.IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean b) {
        editor.putBoolean(Constant.IS_LOGGED_IN, b).apply();
    }

    public String getAppointmentMode() {
        return sharedPreferences.getString(Constant.APPOINTMENT_MODE, "Both (Online & Offline)");
    }

    public void setAppointmentMode(String online) {
        editor.putString(Constant.APPOINTMENT_MODE, online).apply();
    }

    public int getDoctorId() {
        return sharedPreferences.getInt(Constant.DOCTOR_ID, 0);
    }

    public void setDoctorId(int doctorId) {
        editor.putInt(Constant.DOCTOR_ID, doctorId).apply();
    }

    public String isOnline() {
        return sharedPreferences.getString(Constant.IS_ONLINE, "true");
    }

    public void setOnline(String status) {
        editor.putString(Constant.IS_ONLINE, status).apply();
    }

    public void addNotification(String notification) {

        // get old notifications
        String oldNotifications = getNotifications();

        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(Constant.NOTIFICATIONS, oldNotifications);
        editor.commit();
    }

    public String getNotifications() {
        return sharedPreferences.getString(Constant.NOTIFICATIONS, null);
    }

    public void clearNotifications() {
        editor.clear().apply();
    }

    public void setSubProblem(String s) {
        editor.putString(Constant.SUB_PROBLEM, s).apply();
    }

    public String getSubProblem() {
        return sharedPreferences.getString(Constant.SUB_PROBLEM, "");
    }

    public void setProfileImage(String image) {
        editor.putString(Constant.IMAGE, image).apply();
    }

    public String getProfileImage() {
        return sharedPreferences.getString(Constant.IMAGE, "");
    }

    public boolean isVisited() {
        return sharedPreferences.getBoolean(Constant.VISIT, false);
    }

    public void setVisited(boolean b) {
        editor.putBoolean(Constant.VISIT, b).apply();
    }

    public void setAffectedArea(String s) {
        editor.putString(Constant.AFFECTED_AREA, s).apply();
    }

    public String getAffectedArea() {
        return sharedPreferences.getString(Constant.AFFECTED_AREA, "");
    }

    public String getDoctorPhoto() {
        return sharedPreferences.getString(Constant.DOCTOR_PHOTO, "");
    }

    public void setDoctorPhoto(String photo) {
        editor.putString(Constant.DOCTOR_PHOTO, photo).apply();
    }

    public boolean isLoginSkipped() {
        return sharedPreferences.getBoolean(Constant.IS_SKIPPED, false);
    }

    public void setLoginSkipped(boolean check) {
        editor.putBoolean(Constant.IS_SKIPPED, check).apply();
    }

    public boolean isFirstTimeLogin() {
        return sharedPreferences.getBoolean(Constant.IS_FIRST_TIME, true);
    }

    public void setFirstTimeLogin(boolean b) {
        editor.putBoolean(Constant.IS_FIRST_TIME, b).apply();
    }
}
