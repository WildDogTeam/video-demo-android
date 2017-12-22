package com.wilddog.conversation.holders;

import com.wilddog.conversation.floatingwindow.WindowService;
import com.wilddog.video.base.LocalStream;
import com.wilddog.video.call.RemoteStream;

/**
 * Created by fly on 17-10-9.
 */

public class StreamsHolder {
    private static LocalStream localStream;
    private static RemoteStream remoteStream;
    private static WindowService.MyBinder myBinder;

    public static LocalStream getLocalStream() {
        return localStream;
    }

    public static RemoteStream getRemoteStream() {
        return remoteStream;
    }

    public static void setLocalStream(LocalStream localStream) {
        StreamsHolder.localStream = localStream;
    }

    public static void setRemoteStream(RemoteStream remoteStream) {
        StreamsHolder.remoteStream = remoteStream;
    }

    public static void setMyBinder(WindowService.MyBinder myBinder) {
        StreamsHolder.myBinder = myBinder;
    }

    public static WindowService.MyBinder getMyBinder() {
        return myBinder;
    }
}
