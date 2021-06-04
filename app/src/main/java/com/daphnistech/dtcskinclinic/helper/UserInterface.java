package com.daphnistech.dtcskinclinic.helper;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;

public interface UserInterface {

    String BASE_URL = "https://dtcskinclinic.techvkt.com/v1/";

    @FormUrlEncoded
    @POST("checkDoctor")
    Call<String> isDoctorExists(
            @Field("mobile") String mobile,
            @Field("fcmToken") String token,
            @Field("deviceToken") String deviceToken
    );

    @FormUrlEncoded
    @POST("checkPatient")
    Call<String> isPatientExists(
            @Field("mobile") String mobile,
            @Field("fcmToken") String token,
            @Field("deviceToken") String deviceToken
    );

    @FormUrlEncoded
    @POST("login/doctor")
    Call<String> loginDoctor(
            @Field("name") String name,
            @Field("mobile") String mobile,
            @Field("age") String age,
            @Field("gender") String gender,
            @Field("designation") String designation,
            @Field("consultation_fees") String consultationFees,
            @Field("fcm_token") String token,
            @Field("device_token") String deviceToken
    );

    @POST("login/patient")
    @Multipart
    Call<String> loginPatient(
            @Part("name") RequestBody name,
            @Part("mobile") RequestBody mobile,
            @Part("age") RequestBody age,
            @Part("gender") RequestBody gender,
            @Part("address") RequestBody address,
            @Part("state") RequestBody state,
            @Part("pin") RequestBody pin,
            @Part("is_online") RequestBody is_online,
            @Part("fcm_token") RequestBody token,
            @Part("device_token") RequestBody deviceToken,
            @Part MultipartBody.Part image
    );

    @POST("updatePatient")
    @Multipart
    Call<String> updatePatient(
            @Part("patient_id") RequestBody patient_id,
            @Part("mobile") RequestBody mobile,
            @Part("name") RequestBody name,
            @Part("age") RequestBody age,
            @Part("gender") RequestBody gender,
            @Part("address") RequestBody address,
            @Part("state") RequestBody state,
            @Part("pin") RequestBody pin,
            @Part MultipartBody.Part image);

    @GET("getAllDoctors")
    Call<String> getAllDoctors();

    @FormUrlEncoded
    @POST("addPreviousDiseases")
    Call<String> addPreviousDisease(
            @Field("patient_id") int patient_id,
            @Field("is_diabetes") boolean is_diabetes,
            @Field("is_hyper_tension") boolean is_hyper_tension,
            @Field("is_thyroid") boolean is_thyroid,
            @Field("is_drug_allergy") boolean is_drug_allergy,
            @Field("drug_allergy_remarks") String drug_allergy_remarks
    );

    @POST("addCurrentDiseases")
    @Multipart
    Call<String> addCurrentDiseases(
            @Part("patient_id") RequestBody patient_id,
            @Part("disease") RequestBody disease,
            @Part("age") RequestBody age,
            @Part("disease_type") RequestBody disease_type,
            @Part("sub_problem") RequestBody subProblem,
            @Part("comments") RequestBody comments,
            @Part MultipartBody.Part pic1,
            @Part MultipartBody.Part pic2,
            @Part MultipartBody.Part pic3,
            @Part MultipartBody.Part pdf,
            @Part MultipartBody.Part affectedArea
    );

    @FormUrlEncoded
    @POST("deleteDisease")
    Call<String> deleteDisease(
            @Field("patient_id") int patient_id,
            @Field("disease") String disease
    );

    @FormUrlEncoded
    @POST("addAppointment")
    Call<String> addAppointment(
            @Field("name") String name,
            @Field("patient_id") int patient_id,
            @Field("doctor_id") int doctor_id,
            @Field("payment_id") int payment_id,
            @Field("appointment_mode") String appointment_mode,
            @Field("appointment_date") String appointment_date,
            @Field("appointment_time") String appointment_time,
            @Field("appointment_status") String appointment_status,
            @Field("remarks") String remarks);

    @FormUrlEncoded
    @POST("getPatientAppointments")
    Call<String> getPatientAppointments(
            @Field("patient_id") int patient_id
    );

    @FormUrlEncoded
    @POST("getDoctorAppointments")
    Call<String> getDoctorAppointments(
            @Field("doctor_id") int doctor_id
    );

    @FormUrlEncoded
    @POST("getPatientChatList")
    Call<String> getPatientChatList(
            @Field("patient_id") int patient_id
    );

    @FormUrlEncoded
    @POST("getDoctorChatList")
    Call<String> getDoctorChatList(
            @Field("doctor_id") int doctor_id
    );

    @FormUrlEncoded
    @POST("getPatientsList")
    Call<String> getPatientsList(
            @Field("doctor_id") int doctor_id
    );

    @FormUrlEncoded
    @POST("getDoctorsList")
    Call<String> getDoctorsList(
            @Field("patient_id") int patient_id
    );

    @FormUrlEncoded
    @POST("addTransaction")
    Call<String> addTransaction(
            @Field("transaction_id") int transaction_id,
            @Field("patient_id") int patient_id,
            @Field("doctor_id") int doctor_id,
            @Field("transaction_amount") int transaction_amount,
            @Field("transaction_date") String transaction_date,
            @Field("transaction_time") String transaction_time,
            @Field("transaction_status") String transaction_status
    );

    @FormUrlEncoded
    @POST("getPatientTransactions")
    Call<String> getPatientTransactions(
            @Field("patient_id") int patient_id
    );

    @FormUrlEncoded
    @POST("getDoctorTransactions")
    Call<String> getDoctorTransactions(
            @Field("doctor_id") int userID);

    @POST("sendMessage")
    @Multipart
    Call<String> sendMessage(
            @Part("patient_id") RequestBody patient_id,
            @Part("doctor_id") RequestBody doctor_id,
            @Part("sender") RequestBody sender_id,
            @Part("name") RequestBody name,
            @Part("message_type") RequestBody messageType,
            @Part("message_body") RequestBody messageBody,
            @Part MultipartBody.Part image,
            @Part("timestamp") RequestBody timestamp,
            @Part("unread_count") RequestBody count
    );

    @FormUrlEncoded
    @POST("getConversation")
    Call<String> getConversation(
            @Field("patient_id") int patient_id,
            @Field("doctor_id") int doctor_id,
            @Field("type") String type
    );


    @GET("309488-img_20210508_193420076.jpg")
    Call<String> getImage();

    @FormUrlEncoded
    @POST("putStatus")
    Call<String> putStatus(
            @Field("id") int id,
            @Field("type") String type,
            @Field("is_online") String status
    );

    @FormUrlEncoded
    @POST("markAppointmentClosed")
    Call<String> markAppointmentClosed(
            @Field("appointment_id") int appointment_id
    );

    @FormUrlEncoded
    @POST("markAppointmentOpen")
    Call<String> markAppointmentOpen(
            @Field("appointment_id") int appointment_id
    );

    @FormUrlEncoded
    @POST("getPatientDetails")
    Call<String> getPatientDetails(
            @Field("patient_id") int patient_id
    );
}
