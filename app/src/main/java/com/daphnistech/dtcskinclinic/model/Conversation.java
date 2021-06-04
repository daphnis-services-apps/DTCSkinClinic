package com.daphnistech.dtcskinclinic.model;

public class Conversation {
    String senderId;
    String messageBody;
    String messageType;
    String image;
    String timeStamp;

    public Conversation(String senderId, String messageType, String messageBody, String image, String timeStamp) {
        this.senderId = senderId;
        this.messageBody = messageBody;
        this.messageType = messageType;
        this.image = image;
        this.timeStamp = timeStamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
