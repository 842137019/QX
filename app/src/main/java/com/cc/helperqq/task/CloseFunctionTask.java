package com.cc.helperqq.task;

import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsListView;

import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.Utils;
import com.cc.helperqq.service.HelperQQService;

import java.util.List;

/**
 * 關閉功能
 * Created by fangying on 2017/9/21.
 */

public class CloseFunctionTask {
    private HelperQQService service;
    private Handler handler;

    public CloseFunctionTask(HelperQQService service, Handler handler) {
        this.service = service;
        this.handler = handler;
    }

    public void closeFunction() {
        if( !Utils.isTragetActivity(Constants.QQ_HOME_ACTIVITY)){
            Utils.startPage(Constants.QQ_HOME_ACTIVITY);
            Utils.sleep(5000L);
        }
//        String[] types = Utils.splitString(type);
//        Utils.clickCompone(Utils.findViewByText(service, "返回"));
//        Utils.sleep(2000);
//
//        Utils.clickCompone(Utils.findViewById(service, "com.tencent.mobileqq:id/ivTitleBtnLeft"));
//        Utils.sleep(2000);
//
//        Utils.swipeUp("1009 926 670 926");
//        Utils.sleep(2000);

        Utils.clickCompone(Utils.findViewByText(service, "动态"));
        Utils.sleep(2000);

        Utils.clickCompone(Utils.findViewByText(service, "更多"));
        Utils.sleep(2000);

        boolean isfindOpen = true;
        while (isfindOpen) {
            if (Utils.findViewByText(service, "已开启的功能") != null) {
                Utils.clickCompone(Utils.findViewByText(service, "已开启的功能"));
                Utils.sleep(1000);
                isfindOpen = false;
            } else {
                Utils.pressSmallScrollDown();
                Utils.sleep(2000);
            }
        }

        AccessibilityNodeInfo titleNamae = Utils.findViewByTextMatch(service, "在动态中显示");
        if (titleNamae != null) {
            List<AccessibilityNodeInfo> list = Utils.findViewListByType(service, AbsListView.class.getName());
            List<AccessibilityNodeInfo> listtxt = Utils.findViewListById(service,"com.tencent.mobileqq:id/letsTextView");
            if (listtxt!=null){
                LogUtils.logInfo(" listtxt size = "+listtxt.size()+" list size = "+list.size());
                for (int i = 0; i<listtxt.size();i++){
                    Utils.clickComponeByXY(listtxt.get(0));
                    Utils.sleep(3000L);

                    Utils.clickCompone(Utils.findViewByText(service, "关闭"));
                    Utils.sleep(2000);

                    AccessibilityNodeInfo closeInfo = Utils.findViewByText(service, "任然关闭");
                    if (closeInfo != null) {
                        Utils.clickCompone(closeInfo);
                        Utils.sleep(1000);

                        Utils.clickCompone(Utils.findViewByText(service, "返回"));
                        Utils.sleep(1000);
                    }
                }
                Utils.clickComponeByXY(Utils.findViewByText(service,"更多"));
                Utils.sleep(3000L);
                Utils.clickComponeByXY(Utils.findViewByText(service,"动态"));
                Utils.sleep(3000L);
                handler.sendEmptyMessage(1);
            }
        }

//        for (int i = 0; i < types.length; i++) {
//            Utils.clickCompone(Utils.findViewByText(service, types[i]));
//            Utils.sleep(2000);
//
//            Utils.clickCompone(Utils.findViewByText(service, "关闭"));
//            Utils.sleep(2000);
//
//            AccessibilityNodeInfo closeInfo = Utils.findViewByText(service, "任然关闭");
//            if (closeInfo != null) {
//                Utils.clickCompone(closeInfo);
//                Utils.sleep(1000);
//
//                Utils.clickCompone(Utils.findViewByText(service, "返回"));
//                Utils.sleep(1000);
//            }
//        }
    }
}
