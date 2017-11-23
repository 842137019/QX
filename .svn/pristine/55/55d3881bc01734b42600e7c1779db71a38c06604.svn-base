package com.cc.helperqq.task;

import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ProgressBar;

import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.Utils;
import com.cc.helperqq.service.HelperQQService;

/**
 * 清空消息列表
 * Created by fangying on 2017/10/30.
 */

public class ClearChatRecordTask {

    private HelperQQService service;
    private Handler handler;

    public ClearChatRecordTask(HelperQQService service, Handler handler) {
        this.service = service;
        this.handler = handler;
    }

    /***
     *
     * 清空消息列表
     *
     */
    public void emptyRecords() {
        if( !Utils.isTragetActivity(Constants.QQ_HOME_ACTIVITY)){
            Utils.startPage(Constants.QQ_HOME_ACTIVITY);
            Utils.sleep(5000L);
        }
        AccessibilityNodeInfo nodeInfo = Utils.findViewByDesc(service, "帐户及设置");
        if (nodeInfo != null) {
            Utils.clickCompone(nodeInfo);
            Utils.sleep(2000);

            AccessibilityNodeInfo setting = Utils.findViewByText(service, "设置");
            if (setting != null) {
                Utils.clickCompone(setting);
                Utils.sleep(2000);

                Utils.clickCompone(Utils.findViewByText(service, "聊天记录"));
                Utils.sleep(2000);

                Utils.clickCompone(Utils.findViewByText(service, "清空所有聊天记录"));
                Utils.sleep(2000);

                Utils.clickCompone(Utils.findViewByText(service, "清空所有聊天记录"));
                Utils.sleep(2000);

                if (Utils.findViewByType(service, ProgressBar.class.getName()) != null) {
                    Utils.sleep(6000L);
                }

                Utils.clickCompone(Utils.findViewByText(service, "设置"));
                Utils.sleep(2000);

                Utils.clickCompone(Utils.findViewByText(service, "返回"));
                Utils.sleep(2000);
                LogUtils.logInfo("  清空消息列表 完成  ");
                boolean isfind = true;
                while (isfind) {
                    AccessibilityNodeInfo accountinfo = Utils.findViewByDesc(service, "帐户及设置");
                    if (accountinfo != null) {
                        isfind = false;
                    } else {
                        Utils.pressBack(service);
                        Utils.sleep(2000);
                    }
                }
                LogUtils.logInfo("清空消息列表執行完成");
                handler.sendEmptyMessage(1);
            }
        } else {
            LogUtils.logInfo("清空消息列表執行完成");
            handler.sendEmptyMessage(1);
        }
    }
}
