package com.wilddog.conversation.wilddog;

import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.video.call.Conversation;

/**
 * Created by fly on 17-6-13.
 */

public class WilddogVideoManager {
    private static Conversation mConversation;
    private static UserInfo user;

    public static Conversation getConversation(){
        return mConversation;
    }
    public static void saveConversation(Conversation conversation){
        mConversation = conversation;
    }

    public static void clearConversation(){
        mConversation = null;
    }


    public static void setWilddogUser(UserInfo user){

        WilddogVideoManager.user = user;
    }

    public static UserInfo getRemoteUser() {
        return user;
    }
}
