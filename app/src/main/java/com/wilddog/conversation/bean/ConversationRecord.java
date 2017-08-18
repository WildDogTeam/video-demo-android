package com.wilddog.conversation.bean;

/**
 * Created by fly on 17-8-17.
 */

public class ConversationRecord {
    private String localId;
    private String remoteId;
    private String timeStamp;
    private String nickName;
    private String photoUrl;
    private String duration;

    public ConversationRecord() {
    }

    @Override
    public String toString() {
        return "ConversationRecord{" +
                "localId='" + localId + '\'' +
                ", remoteId='" + remoteId + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", nickName='" + nickName + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
