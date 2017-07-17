package com.wilddog.conversationdemo.wilddogAuth;

import com.wilddog.video.IncomingInvite;

/**
 * Created by fly on 17-6-13.
 */

public class WilddogVideoManager {
    private static IncomingInvite mIncomingInvite;

    public static IncomingInvite getIncomingInvite(){
        return mIncomingInvite;
    }
    public static void  saveIncomingInvite(IncomingInvite incomingInvite){
        mIncomingInvite = incomingInvite;
    }

    public static void clearIncomingInvite(){
        mIncomingInvite = null;
    }
}
