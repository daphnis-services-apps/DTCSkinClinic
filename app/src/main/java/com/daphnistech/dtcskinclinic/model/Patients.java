package com.daphnistech.dtcskinclinic.model;

public class Patients {
    int appointment_id;
    int patient_id;
    String name;
    String age;
    String isOnline;
    int unreadCount;

    public Patients(int appointment_id, int patient_id, String name, String age, String isOnline, int unreadCount) {
        this.appointment_id = appointment_id;
        this.patient_id = patient_id;
        this.name = name;
        this.age = age;
        this.isOnline = isOnline;
        this.unreadCount = unreadCount;
    }

    public int getAppointmentId() {
        return appointment_id;
    }

    public void setAppointmentId(int appointment_id) {
        this.appointment_id = appointment_id;
    }

    public int getPatientId() {
        return patient_id;
    }

    public void setPatientId(int patient_id) {
        this.patient_id = patient_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(String isOnline) {
        this.isOnline = isOnline;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
