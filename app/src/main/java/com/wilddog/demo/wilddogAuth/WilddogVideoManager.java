package com.wilddog.demo.wilddogAuth;

import com.wilddog.video.Conversation;

/**
 * Created by fly on 17-6-13.
 */

public class WilddogVideoManager {
    private static Conversation mConversation;

    public static Conversation getConversation(){
        return mConversation;
    }
    public static void saveConversation(Conversation conversation){
        mConversation = conversation;
    }

    public static void clearConversation(){
        mConversation = null;
    }
}
