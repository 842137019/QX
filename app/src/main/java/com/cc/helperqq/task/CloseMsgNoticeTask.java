package com.cc.helperqq.task;

import android.graphics.Rect;
import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;

import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.Utils;
import com.cc.helperqq.service.HelperQQService;

import java.util.List;

/**
 * 关闭消息通知
 * Created by fangying on 2017/10/16.
 */

public class CloseMsgNoticeTask {

    private HelperQQService service;
    private Handler handler;

    public CloseMsgNoticeTask(HelperQQService service, Handler handler) {
        this.service = service;
        this.handler = handler;
    }

    public void closeNoticeGetInfo() {
        if( !Utils.isTragetActivity(Constants.QQ_HOME_ACTIVITY)){
            Utils.startPage(Constants.QQ_HOME_ACTIVITY);
            Utils.sleep(5000L);
        }
        closeNotice();
    }

    private void closeNotice() {
        AccessibilityNodeInfo accountinfo = Utils.findViewByDesc(service, "帐户及设置");
        if (accountinfo == null) {
            handler.sendEmptyMessage(1);
            return;
        }
        Utils.clickCompone(accountinfo);
        Utils.sleep(3 * 1000L);
        AccessibilityNodeInfo setting = Utils.findViewByText(service, "设置");
        if (setting != null) {
            Utils.clickCompone(setting);
            Utils.sleep(2000);
            closeNoticeAndSound();

            auxiliaryFunction();

            breakReturn();
        }
    }

    /***
     * 关闭消息通知和声音
     */
    private void closeNoticeAndSound() {
        Utils.clickCompone(Utils.findViewByText(service, "消息通知"));
        Utils.sleep(2000);
        List<AccessibilityNodeInfo> compoundBtn = Utils.findViewListByType(service, CompoundButton.class.getName());//android.widget.CompoundButton
        switchBtn(compoundBtn);
        turnSoundOff();
    }

    /**
     * 关闭辅助功能
     */
    private void auxiliaryFunction() {
        Utils.clickCompone(Utils.findViewByText(service, "辅助功能"));
        Utils.sleep(2000);

        List<AccessibilityNodeInfo> compoundBtns = Utils.findViewListByType(service, CompoundButton.class.getName());
        AccessibilityNodeInfo btn = null;
        for (int i = 0; i < compoundBtns.size(); i++) {
            switch (i) {
                case 0:
                case 1:
                    btn = compoundBtns.get(i);
                    if (btn.isChecked()) {
                        Rect rect = new Rect();
                        btn.getBoundsInScreen(rect);
                        LogUtils.logError("x1:" + rect.left + "y1:" + rect.top + "x2:" + rect.right + "y2:" + rect.bottom);
                        int x = (rect.left + rect.right) / 2;
                        int y = (rect.top + rect.bottom) / 2;
                        Utils.tapScreenXY(x + " " + y);
                        Utils.sleep(2500L);
                        btn = null;
                    }
                    break;
            }
        }

        if ((btn = Utils.findViewByTextMatch(service, "日迹自动播放")) != null) {
            Utils.clickCompone(btn);
            Utils.sleep(2000L);

            AccessibilityNodeInfo checkwifi = Utils.findViewByDesc(service, "仅WiFi 未选中");
            if (checkwifi != null) {
                Utils.clickCompone(checkwifi);
                Utils.sleep(2000L);
            }
            Utils.clickCompone(Utils.findViewByText(service, "辅助功能"));//com.tencent.mobileqq:id/ivTitleBtnLeft
            Utils.sleep(2000);
        }

        if (Utils.findViewByTextMatch(service, "设置") != null) {
            Utils.clickCompone(Utils.findViewByTextMatch(service, "设置"));
            Utils.sleep(2000L);
        }
    }

    /***
     *
     * 回到主界面
     */
    private void breakReturn() {
        AccessibilityNodeInfo msgNotice = Utils.findViewByTextMatch(service, "设置");
        if (msgNotice != null) {
            Utils.clickCompone(msgNotice);
            Utils.sleep(2000);

//            AccessibilityNodeInfo set= Utils.findViewByDesc(service,"设置");
//            if (set!=null){
//                Utils.clickCompone(set);
//                Utils.sleep(2000);
//            }
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
            LogUtils.logInfo("关闭通知完成");
            handler.sendEmptyMessage(1);
        }
    }

    /***
     * 关闭声音
     */
    private void turnSoundOff() {
        AccessibilityNodeInfo sound = Utils.findViewByText(service, "声音");
        if (sound != null) {
            Utils.clickCompone(sound);
            Utils.sleep(2000);
            List<AccessibilityNodeInfo> compoundBtn = Utils.findViewListByType(service, CompoundButton.class.getName());
            LogUtils.logInfo("关闭声音");
            switchBtn(compoundBtn);

            Utils.clickCompone(Utils.findViewByTextMatch(service, "消息通知"));
            Utils.sleep(2000);

            if (Utils.findViewByTextMatch(service, "设置") != null) {
                Utils.clickCompone(Utils.findViewByTextMatch(service, "设置"));
                Utils.sleep(2000L);
            }
        }
    }

    private void switchBtn(List<AccessibilityNodeInfo> listnode) {
        int size = listnode.size();
        if (listnode != null && size > 0) {
            AccessibilityNodeInfo btn = null;
            for (int i = 0; i < listnode.size(); i++) {
                if (i != 1) {
//                    LogUtils.logError(" size =" + listnode.size() + "   i=" + i + "    btn =" + listnode.get(i));
                    btn = listnode.get(i);
                    if (btn.isChecked()) {
                        Rect rect = new Rect();
                        btn.getBoundsInScreen(rect);
                        LogUtils.logError("x1:" + rect.left + "y1:" + rect.top + "x2:" + rect.right + "y2:" + rect.bottom);
                        int x = (rect.left + rect.right) / 2;
                        int y = (rect.top + rect.bottom) / 2;
                        Utils.tapScreenXY(x + " " + y);
                        Utils.sleep(2500L);
                        btn = null;
                    }
                }
            }
        }
    }

}
