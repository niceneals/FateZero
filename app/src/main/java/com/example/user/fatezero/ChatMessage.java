package com.example.user.fatezero;

import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatMessage {
    Date time;
    String message;
    String device;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

    public ChatMessage(String device, String message) {
        this.time = new Date();
        this.message = message;
        this.device = device;
    }

    public String getTime() {
        return sdf.format(this.time);
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
