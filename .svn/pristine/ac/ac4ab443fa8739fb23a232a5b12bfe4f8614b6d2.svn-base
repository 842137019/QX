package com.cc.helperqq.task;

import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;

import com.cc.helperqq.entity.TaskEntry;
import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.FileUtils;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.QQError;
import com.cc.helperqq.utils.SharePreferencesUtils;
import com.cc.helperqq.utils.Utils;
import com.cc.helperqq.service.HelperQQService;

import java.io.File;

/**
 * 还原
 * Created by fangying on 2017/9/11.
 */

public class ReductionTask {

    private HelperQQService service;
    private Handler handler;
    private QQError error;

    public ReductionTask(HelperQQService service, Handler handler) {
        this.handler = handler;
        this.service = service;
    }

    public void ReductionData(TaskEntry entry) {
        if (entry == null) {
            handler.sendEmptyMessage(2);
            return;
        }
        Utils.exitApp(Constants.APP_NAME);
        String qq_sign = entry.getWx_sign();
        String cachePath = "/sdcard" + File.separator + Constants.CACHE_PATH_NAME;
        FileUtils.createDir(cachePath);
        String cacheInfo = cachePath + File.separator + Constants.QQ_INFO;
        FileUtils.createDir(cacheInfo);

        String cacheFile_data = cacheInfo + File.separator + qq_sign + "_data";
        String cacheFile_cvqq = cacheInfo + File.separator + qq_sign + "_cvqq";
        Utils.sleep(5000L);
        if (!FileUtils.fileIsExists(cacheFile_data)) {
            LogUtils.logError("缓存文件不存在:进入登入页面" + qq_sign);
            Utils.clearData();
            //获取新的QQ初始信息
            String new_data = "/sdcard" + File.separator + Constants.CACHE_PATH_NAME + File.separator + "new_data";
            String new_cvqq = "/sdcard" + File.separator + Constants.CACHE_PATH_NAME + File.separator + "new_cvqq";
            Utils.reductionData(new_data, null, new_cvqq);
        } else {
            Utils.clearData();
            Utils.sleep(3000L);
            Utils.reductionData(cacheFile_data, null, cacheFile_cvqq);
        }
        if (Utils.validateIsModify(service, entry)) {
            Utils.openFlyModel(service);
            Utils.sleep(5000L);
            launcherApp(entry);
        } else {
            LogUtils.logError("xposed失效---");
            Utils.rebootSystem(service);
        }

    }

    private void launcherApp(TaskEntry entry) {
        Utils.launcherApp(service, Constants.APP_NAME);
        Utils.sleep(20 * 1000L);
        LoginTask.loginQQ(service);
        AccessibilityNodeInfo passBtn = Utils.findViewByText(service, Button.class.getName(), "新用户注册");
        if (passBtn != null) {
            LoginTask.login(service, entry);
            boolean isTrue = QQError.validateLogin(service, entry.getWx_sign());
            if (isTrue) {
                reductionOk(entry);
            } else {
                handler.sendEmptyMessage(2);
            }
        } else {
            reductionOk(entry);
        }
    }

    private void reductionOk(TaskEntry entry) {
        LogUtils.logInfo("還原完成");
        SharePreferencesUtils.getInstance(service).setTimeStamp(entry.getCurrent_time());
        SharePreferencesUtils.getInstance(service).setCurrentTime(entry.getRuntime());
        handler.sendEmptyMessage(1);
    }
}

