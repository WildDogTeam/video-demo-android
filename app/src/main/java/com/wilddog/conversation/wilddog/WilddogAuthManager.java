package com.wilddog.conversation.wilddog;


import com.wilddog.conversation.ConversationApplication;
import com.wilddog.wilddogauth.WilddogAuth;

/**
 * Created by fly on 17-6-9.
 */

public class WilddogAuthManager {
    private static WilddogAuth wilddogAuth;
    public static WilddogAuth getWilddogAuth(){
        if(!WilddogManager.IsInit()){
           WilddogManager.initWilddog(ConversationApplication.getContext().getApplicationContext());
        }
        if(wilddogAuth==null){
        wilddogAuth = WilddogAuth.getInstance();}
        return wilddogAuth;
    }



}
