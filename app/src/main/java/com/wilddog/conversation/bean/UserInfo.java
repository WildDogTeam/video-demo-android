package com.wilddog.conversation.bean;

import java.io.Serializable;

/**
 * Created by fly on 17-8-17.
 */

public class UserInfo implements Serializable{
    private String uid;
    private String nickname;
    private String faceurl;
    private String deviceid;
    public UserInfo(){

    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFaceurl() {
        return faceurl;
    }

    public void setFaceurl(String faceurl) {
        this.faceurl = faceurl;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "uid='" + uid + '\'' +
                ", nickname='" + nickname + '\'' +
                ", faceurl='" + faceurl + '\'' +
                ", deviceid='" + deviceid + '\'' +
                '}';
    }
}
