package com.bluelock.tiktokdownloader.util;

import android.content.Context;
import android.widget.Toast;


public class Utils {
    private final Context context;

    public Utils(Context mContext) {
        context = mContext;
    }

    public static void setToast(Context mContext, String str) {
        Toast toast = Toast.makeText(mContext, str, Toast.LENGTH_SHORT);
        toast.show();
    }


}