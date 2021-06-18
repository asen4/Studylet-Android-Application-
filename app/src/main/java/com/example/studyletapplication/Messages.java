package com.example.studyletapplication;

public class Messages {

    public String messageID, from, recipient, date, message, time, type;

    public Messages() {

    }

    public Messages(String messageID, String from, String recipient, String date, String message, String time, String type) {
        this.messageID = messageID;
        this.from = from;
        this.recipient = recipient;
        this.date = date;
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
