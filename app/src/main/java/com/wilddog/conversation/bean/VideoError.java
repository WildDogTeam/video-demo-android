package com.wilddog.conversation.bean;

/**
 * Created by fly on 17-12-6.
 */

public class VideoError {
    private int errCode;
    private String message;
    public VideoError(int errCode, String message) {
       this.errCode = errCode;
       this.message = message;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getMessage() {
        return message;
    }
}
