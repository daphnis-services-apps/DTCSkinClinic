package com.daphnistech.dtcskinclinic.model;

public class MyPatientDoctor {
    int id;
    int appointmentId;
    String name;
    String title;
    String appointmentStatus;

    public MyPatientDoctor(int id, int appointmentId, String name, String title, String appointmentStatus) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.name = name;
        this.title = title;
        this.appointmentStatus = appointmentStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }
}
