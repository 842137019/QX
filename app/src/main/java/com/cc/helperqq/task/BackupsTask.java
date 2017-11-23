package com.cc.helperqq.task;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import com.cc.helperqq.entity.TaskEntry;
import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.FileUtils;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.QQError;
import com.cc.helperqq.utils.SharePreferencesUtils;
import com.cc.helperqq.utils.ShellUtils;
import com.cc.helperqq.utils.Utils;
import com.cc.helperqq.http.HttpHandler;
import com.cc.helperqq.http.HttpTask;
import com.cc.helperqq.service.HelperQQService;

import java.io.File;
import java.util.List;

/**
 * 备份
 * Created by fangying on 2017/9/11.
 */

public class BackupsTask {
    private HelperQQService service;
    private Handler handler;

    public BackupsTask(HelperQQService service, Handler handler) {
        this.handler = handler;
        this.service = service;
    }

    public void backups(final TaskEntry entry) {
       if( !Utils.isTragetActivity(Constants.QQ_HOME_ACTIVITY)){
           Utils.startPage(Constants.QQ_HOME_ACTIVITY);
           Utils.sleep(3000L);
        }
        sleepTime();

        String name = "";
        AccessibilityNodeInfo nodeInfo = Utils.findViewByDesc(service, "帐户及设置");
        if (nodeInfo == null) {
            handler.sendEmptyMessage(1);
            return;
        }
        Utils.clickCompone(nodeInfo);
        Utils.sleep(3000);
        AccessibilityNodeInfo nickName = Utils.findViewById(service, "com.tencent.mobileqq:id/nickname");
        if (nickName != null && !TextUtils.isEmpty(nickName.getText())) {
            name = nickName.getText()+"";
            nickName.recycle();
            LogUtils.logError(" nickname=  " + name);
        }
        Utils.clickCompone(Utils.findViewById(service, "com.tencent.mobileqq:id/head_layout"));
        Utils.sleep(3000);
        if (Utils.isTragetActivity(Constants.QQ_USERINFO_ACTIVITY)) {
            List<AccessibilityNodeInfo> list = Utils.findViewListByType(service, TextView.class.getName());
            String nick = "";
            String id = "";
            boolean isfind = true;
            int index = 0;
            while (isfind) {
                if (!TextUtils.isEmpty(list.get(index).getText())) {
                    String nickname = list.get(index).getText().toString();
                    if (nickname.equals(name)) {
                        nick = nickname;
                        id = list.get(index + 2).getText().toString();
                        isfind = false;
                    }
                }
                index++;
            }

            LogUtils.logInfo("  nick = " + nick + " id =" + id);
            Utils.sleep(3000);

            Utils.exitApp(Constants.APP_NAME);
            Utils.sleep(3000L);

            if (entry.getWx_id().equals(id) && Utils.validateIsModify(service, entry)) {
                //备份
                exeBackupOper(entry);
                //上传
                uploadInfo(entry,id,Utils.getDeviceId(service),Utils.getSId(service),"",Utils.getSIM(service));
            } else {
                LogUtils.logError("ID 出错，或设备号不匹配");
                handler.sendEmptyMessage(1);
                return;
            }
        }
    }

    private void sleepTime() {
        boolean isrun = true;
        while (isrun) {
            long time = System.currentTimeMillis() / 1000;
            long oldtime = SharePreferencesUtils.getInstance(service).getTimeStamp();
            long currentTime = SharePreferencesUtils.getInstance(service).getCurrentTime();
            LogUtils.logError(" 等待备份: " + "time=" + time + "  oldtime=" + oldtime + "   currentTime=" + currentTime + "  time - oldtime=" + (time - oldtime));
            if (time - oldtime >= currentTime) {
                SharePreferencesUtils.getInstance(service).setTimeStamp(0);
                SharePreferencesUtils.getInstance(service).setCurrentTime(0);
                isrun = false;
            } else {
                QQError.loginPromptAcitvity(service);
                Utils.sleep(40 * 1000L);
            }
        }
    }

    private void uploadInfo(final TaskEntry entry, String id, String imei, String sid, String mac, String sim){
        HttpHandler.reportLocalInfo(entry.getWx_sign(), imei, null, sid, sim, id, null, null, null, new HttpTask.HttpCallback() {
            @Override
            public void onSuccess(String data) {
                LogUtils.logInfo("上傳數據完成  " + data);
            }

            @Override
            public void onFailure(String errMsg) {
                LogUtils.logError("请求失败！" + errMsg);
                HttpHandler.reportError(entry.getWx_sign(), "上傳數據失败!");
                handler.sendEmptyMessage(2);
            }

            @Override
            public void onFinished() {
                handler.sendEmptyMessage(1);
            }
        });
    }

    private void exeBackupOper(final TaskEntry info) {
            new Thread() {
                @Override
                public void run() {
                    String qq_sign = info.getWx_sign();
                    String cachePath = "/sdcard" + File.separator + Constants.CACHE_PATH_NAME + File.separator + Constants.QQ_INFO;
                    FileUtils.createDir(cachePath);
                    String backups_data = cachePath + File.separator + qq_sign + "_data";
//                    String backups_imqq = cachePath + File.separator + qq_sign + "_imqq";
                    String backups_cvqq = cachePath + File.separator + qq_sign + "_cvqq";
                    Utils.backupsData(backups_data, null, backups_cvqq);
                }

            }.start();
    }

    public void Backup(Context context) {
        context.getPackageName();
//        mContext = context;
//        ApkInfo apkInfo = new ResourceUtil(context).getApkInfo();
//        APP_PATH = new StringBuilder("/data/data/").append(apkInfo.packageName)
//                .toString();
//        SHARED_PREFS = APP_PATH + "/shared_prefs";
//        DATABASES = APP_PATH + "/databases";
//        if (Environment.MEDIA_MOUNTED.equals(Environment
//                .getExternalStorageState())) {
//            BACKUP_PATH = "/sdcard/lurencun/backup";
//        } else {
//            BACKUP_PATH = "/com.lurencun/backup/";
//            Toast.makeText(mContext, "没有检测到SD卡，可能无法备份成功", Toast.LENGTH_SHORT)
//                    .show();
//        }
//        BACKUP_PATH += apkInfo.packageName;
//        BACKUP_DATABASES = BACKUP_PATH + "/database";
//        BACKUP_SHARED_PREFS = BACKUP_PATH + "/shared_prefs";
    }

//    /**
//     * 备份文件
//     *
//     * @return 当且仅当数据库及配置文件都备份成功时返回true。
//     */
//    public boolean doBackup() {
//        return backupDB() && backupSharePrefs();
//    }

//    private boolean backupDB() {
//        return copyDir(DATABASES, BACKUP_DATABASES, "备份数据库文件成功:"
//                + BACKUP_DATABASES, "备份数据库文件失败");
//    }
//
//    private boolean backupSharePrefs() {
//        return copyDir(DATABASES, BACKUP_DATABASES, "备份配置文件成功:"
//                + BACKUP_SHARED_PREFS, "备份配置文件失败");
//    }

//    /**
//     * 恢复
//     *
//     * @return 当且仅当数据库及配置文件都恢复成功时返回true。
//     */
//    public boolean doRestore() {
//        return restoreDB() && restoreSharePrefs();
//    }

//    private boolean restoreDB() {
//        return copyDir(BACKUP_DATABASES, DATABASES, "恢复数据库文件成功", "恢复数据库文件失败");
//    }
//
//    private boolean restoreSharePrefs() {
//        return copyDir(BACKUP_SHARED_PREFS, SHARED_PREFS, "恢复配置文件成功",
//                "恢复配置文件失败");
//    }
//
//    private final void showToast(String msg) {
//        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
//    }

    /**
     * 复制目录
     *
     * @param srcDir
     *            源目录
     * @param destDir
     *            目标目录
     * @param successMsg
     *            复制成功的提示语
     * @param failedMsg
     *            复制失败的提示语
     * @return 当复制成功时返回true, 否则返回false。
     */
    private final boolean copyDir(String srcDir, String destDir,
                                  String successMsg, String failedMsg) {
//        try {
//            FileUtils.copyDirectory(new File(srcDir), new File(destDir));
//        } catch (IOException e) {
//            e.printStackTrace();
//            showToast(failedMsg);
//            return false;
//        }
//        showToast(successMsg);
        return true;
    }
}
