package com.cc.helperqq.recver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.Utils;


/**
 *
 * Created by yzq on 2017/3/6.
 */

public class BootRecver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            LogUtils.logInfo("开机启动 ");
            Utils.launcherApp(context, context.getPackageName());
        }
    }
}
