package com.wilddog.conversation.bean;

/**
 * Created by fly on 17-8-17.
 */

public class ConversationRecord {
    private String localId;
    private String remoteId;
    private String timestamp;
    private String nickname;
    private String photoUrl;
    private String duration;

    public ConversationRecord() {
    }

    @Override
    public String toString() {
        return "ConversationRecord{" +
                "localId='" + localId + '\'' +
                ", remoteId='" + remoteId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", nickname='" + nickname + '\'' +
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
