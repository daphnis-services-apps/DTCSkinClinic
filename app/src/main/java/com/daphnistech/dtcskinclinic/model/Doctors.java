package com.daphnistech.dtcskinclinic.model;

public class Doctors {
    int appointment_id;
    int doctorId;
    String name;
    String designation;
    String rating;
    String photo;
    String consultationFees;
    String isOnline;
    int unreadCount;

    public Doctors(int id, String name, String photo, String designation, String rating, String consultationFees) {
        this.doctorId = id;
        this.name = name;
        this.photo = photo;
        this.designation = designation;
        this.rating = rating;
        this.consultationFees = consultationFees;
    }

    public Doctors(int appointment_id, int id, String name, String photo, String designation, String isOnline, int unreadCount) {
        this.appointment_id = appointment_id;
        this.doctorId = id;
        this.name = name;
        this.photo = photo;
        this.designation = designation;
        this.isOnline = isOnline;
        this.unreadCount = unreadCount;
    }

    public int getAppointmentId() {
        return appointment_id;
    }

    public void setAppointmentId(int appointment_id) {
        this.appointment_id = appointment_id;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getConsultationFees() {
        return consultationFees;
    }

    public void setConsultationFees(String consultationFees) {
        this.consultationFees = consultationFees;
    }

    public String isOnline() {
        return isOnline;
    }

    public void setOnline(String online) {
        isOnline = online;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
