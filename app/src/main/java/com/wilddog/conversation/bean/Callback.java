package com.wilddog.conversation.bean;


/**
 * Created by fly on 17-12-6.
 */

public interface Callback<T> {
    void onSuccess(T t);
    void onFailed(VideoError error);
}
