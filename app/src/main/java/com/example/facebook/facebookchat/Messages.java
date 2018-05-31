package com.example.facebook.facebookchat;

public class Messages {
    private String message;
    private String name;
    private String profilePic;
    private long time;
    private String type;
    private String from;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public Messages(String message, String name,String profilePic, long time, String type, String from) {
        this.message = message;
        this.name = name;
        this.profilePic = profilePic;
        this.time = time;
        this.type = type;
        this.from = from;
    }

    public Messages() {}
}
