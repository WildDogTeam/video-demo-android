package com.wilddog.conversation.utils;

import com.wilddog.video.Conversation;
/**
 * Created by fly on 17-6-13.
 */

public class WilddogVideoManager {
    private static Conversation mConversation;

    public static Conversation getIncomingInvite(){
        return mConversation;
    }
    public static void  saveIncomingInvite(Conversation conversation){
        mConversation = conversation;
    }

    public static void clearIncomingInvite(){
        mConversation = null;
    }
}
