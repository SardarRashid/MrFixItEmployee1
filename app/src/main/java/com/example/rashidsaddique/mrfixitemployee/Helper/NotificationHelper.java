package com.example.rashidsaddique.mrfixitemployee.Helper;

import android.content.Context;
import android.content.ContextWrapper;

import java.nio.channels.Channel;

public class NotificationHelper extends ContextWrapper {
    private static final String FIX_IT_ID = "com.example.rashidsaddique.mrfixitemployee.FIXIT";
    private static final String FIX_IT_NAME = "FIXIT employee";
    public NotificationHelper(Context base) {
        super(base);
    }
}
