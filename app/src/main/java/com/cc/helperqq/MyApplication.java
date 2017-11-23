package com.cc.helperqq;

import android.app.Application;
import android.content.Context;

import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.FileUtils;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.Utils;

import org.litepal.LitePalApplication;

import java.io.File;

/**
 * 分风格非官方非官方
 * Created by fangying on 2017/9/4.
 */

public class MyApplication extends LitePalApplication {

    public static boolean debugMode = false;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Utils.initClipManager(this);
        LogUtils.setIsDebug(true);
        cacheFile();
    }

    private void cacheFile() {
        String cachePath = "/sdcard" + File.separator + Constants.CACHE_PATH_NAME;
        FileUtils.createDir(cachePath);
        String cacheiamges = "/sdcard" + File.separator + Constants.CACHE_PATH_NAME + File.separator + Constants.QQ_PHOTO;
        FileUtils.createDir(cacheiamges);
        String cacheInfo = "/sdcard" + File.separator + Constants.CACHE_PATH_NAME + File.separator + Constants.QQ_INFO;
        FileUtils.createDir(cacheInfo);

        String new_data = "/sdcard" + File.separator + Constants.CACHE_PATH_NAME + File.separator + "new_data";
        if (!FileUtils.fileIsExists(new_data)) {
            Utils.exitApp(Constants.APP_NAME);
            Utils.backupsData(new_data, null, null);
        }
    }
}
