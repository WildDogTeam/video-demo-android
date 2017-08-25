package com.wilddog.conversation.bean;

import java.io.Serializable;

/**
 * Created by fly on 17-8-17.
 */

public class UserInfo implements Serializable{
    private String uid;
    private String nickName;
    private String photoUrl;

    public UserInfo(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "uid='" + uid + '\'' +
                ", nickName='" + nickName + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                '}';
    }
}
