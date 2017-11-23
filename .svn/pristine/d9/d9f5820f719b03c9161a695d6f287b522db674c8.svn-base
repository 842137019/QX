package com.cc.helperqq.utils;

import android.util.Log;

/**
 * Created by yzq on 2017/4/18.
 */

public class LogUtils {

    private static final String TAG = "QQHelper";

    private static boolean isDebug = false;

    public static void logInfo(String msg){
        if(isDebug){
            Log.i(TAG,msg);
        }
    }

    public static void logInfo(String msg, Throwable exception){
        if(isDebug){
            Log.i(TAG,msg,exception);
        }
    }

    public static void logError(String msg){
        if(isDebug){
            Log.e(TAG,msg);
        }
    }

    public static void logError(String msg, Throwable exception){
        if(isDebug){
            Log.e(TAG,msg,exception);
        }
    }

    public static void setIsDebug(boolean isDebug) {
        if(isDebug){
            LogUtils.isDebug = isDebug;
        }
    }
}
