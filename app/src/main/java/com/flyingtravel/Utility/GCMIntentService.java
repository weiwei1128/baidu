package com.flyingtravel.Utility;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

/**
 * Created by wei on 2016/5/12.
 */
public class GCMIntentService extends GCMBaseIntentService{

    protected GCMIntentService(String senderId) {
        super(senderId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {

    }

    @Override
    protected void onError(Context context, String s) {

    }

    @Override
    protected void onRegistered(Context context, String s) {

    }

    @Override
    protected void onUnregistered(Context context, String s) {

    }
}
