package com.daphnistech.dtcskinclinic.model;

public class Transaction {
    int paymentId;
    int transactionId;
    int appointmentId;
    int transactionAmount;
    String transactionDate;
    String transactionTime;
    String transactionStatus;

    public Transaction(int paymentId, int transactionId, int appointmentId, int transactionAmount, String transactionDate, String transactionTime, String transactionStatus) {
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.appointmentId = appointmentId;
        this.transactionAmount = transactionAmount;
        this.transactionDate = transactionDate;
        this.transactionTime = transactionTime;
        this.transactionStatus = transactionStatus;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(int transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}
