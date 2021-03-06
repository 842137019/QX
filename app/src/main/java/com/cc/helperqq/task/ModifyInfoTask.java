package com.cc.helperqq.task;

import android.os.Handler;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;

import com.cc.helperqq.entity.TaskEntry;
import com.cc.helperqq.entity.UserInfoEntity;
import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.FileUtils;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.Utils;
import com.cc.helperqq.http.HttpHandler;
import com.cc.helperqq.http.HttpTask;
import com.cc.helperqq.service.HelperQQService;

import java.io.File;


/**
 * 修改個人信息
 * Created by fangying on 2017/9/4.
 */

public class ModifyInfoTask {

    private HelperQQService service;
    private Handler handler;

    public ModifyInfoTask(HelperQQService service, Handler handler) {
        this.handler = handler;
        this.service = service;
    }


    public void modifyInfo(final TaskEntry taskEntry) {
        if( !Utils.isTragetActivity(Constants.QQ_HOME_ACTIVITY)){
            Utils.startPage(Constants.QQ_HOME_ACTIVITY);
            Utils.sleep(5000L);
        }
        HttpHandler.requestModifyPersonal(taskEntry.getWx_sign(), new HttpTask.HttpCallback() {
            @Override
            public void onSuccess(String data) {
                LogUtils.logInfo("个人信息=" + data);
                if (!TextUtils.isEmpty(data)) {
                    UserInfoEntity infoEntity = Utils.getGson().fromJson(data, UserInfoEntity.class);
                    modifyInfo(infoEntity);
                } else {
                    HttpHandler.reportError(taskEntry.getWx_id(), "个人信息数据为空!");
                    handler.sendEmptyMessage(1);
                }
            }

            @Override
            public void onFailure(String errMsg) {
                LogUtils.logError("个人信息数据请求失败!!   " + errMsg);
                HttpHandler.reportError(taskEntry.getWx_sign(), "个人信息数据请求失败!");
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void modifyInfo(UserInfoEntity infoEntity) {
        //com.tencent.mobileqq:id/conversation_head
        if( !Utils.isTragetActivity(Constants.QQ_HOME_ACTIVITY)){
            Utils.startPage(Constants.QQ_HOME_ACTIVITY);
            Utils.sleep(3000L);
        }
        AccessibilityNodeInfo nodeInfo = Utils.findViewByDesc(service, "帐户及设置");
        if (nodeInfo == null) {
            handler.sendEmptyMessage(1);
            return;
        }
        Utils.clickCompone(nodeInfo);
        Utils.sleep(2000);
        Utils.clickCompone(Utils.findViewById(service, "com.tencent.mobileqq:id/head_layout"));
        Utils.sleep(2000);

        Utils.clickCompone(Utils.findViewByText(service, "编辑资料"));
        Utils.sleep(2000);

        ModifyAutograph(infoEntity.getSignature());
        modifyNickName(infoEntity.getNickname());
        settingSex(infoEntity);
        modifyBirthday(infoEntity.getBirthday());
        //"http://img2.woyaogexing.com/2017/09/15/249e6094b234877d!400x400_big.jpg"
        LogUtils.logInfo("infoEntity.getHeadurl()=" + infoEntity.getHead_url());
        downloadPicture(infoEntity.getHead_url());
    }

    /**
     * 设置生日
     */
    private void modifyBirthday(String bthday) {
        LogUtils.logInfo("  设置生日  ");
        if (!TextUtils.isEmpty(bthday)) {
            AccessibilityNodeInfo birthday = Utils.findViewByText(service, "生日");
            if (birthday != null) {
                String birthdayText = birthday.getParent().getChild(5).getChild(1).getText().toString();
                if (birthdayText.equals("你的生日")) {
                    birthdayText = "1990-1-1";
                }
                String[] birthes = birthdayText.split("-");
                String[] newBirthes = bthday.split("-");
                if (!birthdayText.equals(bthday)) {
                    Utils.clickCompone(birthday);
                    Utils.sleep(3000);
                    int newYear = Integer.parseInt(newBirthes[0]);
                    int newMonth = Integer.parseInt(newBirthes[1]);
                    int newDay = Integer.parseInt(newBirthes[2]);
                    int oldYear = Integer.parseInt(birthes[0]);
                    int oldMonth = Integer.parseInt(birthes[1]);
                    int oldDay = Integer.parseInt(birthes[2]);
                    //month     day
                    LogUtils.logError("oldYear =" + Integer.parseInt(birthes[0]));
                    AccessibilityNodeInfo nodeInfo = Utils.findViewById(service, "com.tencent.mobileqq:id/action_sheet_actionView");
                    if (nodeInfo != null) {
                        switch (Utils.getSystemModel()) {
                            case Constants.HONOR_PHONE_MODEL:
                                //年 145 1124  月 390 1081  日 605 1081    90年以后
                                settingDateHonor(newYear, newMonth, newDay, oldYear, oldMonth, oldDay);
                                //年 134 944  月 391 944  日 608 944    90年以前
                                break;

                            case Constants.RAMOS_PHONE_MODEL:
                                settingDateRamos(newYear, newMonth, newDay, oldYear, oldMonth, oldDay);
                                break;
                        }
                        Utils.clickCompone(Utils.findViewByText(service, "完成"));
                        Utils.sleep(1500);

                        AccessibilityNodeInfo back = Utils.findViewByText(service, "返回");//com.tencent.mobileqq:id/ivTitleBtnLeft
                        if (back != null) {
                            Utils.clickCompone(back);
                            Utils.sleep(2000);
                        }
                    }
                }
            }
        }
    }

    /***
     *
     * 设置年月日  （华为）
     * @param newYear
     * @param newMonth
     * @param newDay
     * @param oldYear
     * @param oldMonth
     * @param oldDay
     */
    private void settingDateHonor(int newYear, int newMonth, int newDay, int oldYear, int oldMonth, int oldDay) {
        if (newYear > oldYear) {
            for (int i = 0; i < newYear - oldYear; i++) {
                Utils.tapScreenXY("138 1073");
                Utils.sleep(2000);
            }
        } else if (newYear < oldYear) {
            for (int i = 0; i < oldYear - newYear; i++) {
                Utils.tapScreenXY("134 944");
                Utils.sleep(2000);
            }
        }

        if (newMonth > oldMonth) {
            for (int i = 0; i < newMonth - oldMonth; i++) {
                Utils.tapScreenXY("390 1081");
                Utils.sleep(2000);
            }
        } else if (oldMonth > newMonth) {
            for (int i = 0; i < oldMonth - newMonth; i++) {
                Utils.tapScreenXY("396 961");
                Utils.sleep(2000);
            }
        }

        if (newDay > oldDay) {
            for (int i = 0; i < newDay - oldDay; i++) {
                Utils.tapScreenXY("605 1081");
                Utils.sleep(2000);
            }
        } else if (oldDay > newDay) {
            for (int i = 0; i < oldDay - newDay; i++) {
                Utils.tapScreenXY("617 964");
                Utils.sleep(2000);
            }
        }
    }

    /***
     * 设置年月日  （蓝魔）
     * @param newYear
     * @param newMonth
     * @param newDay
     * @param oldYear
     * @param oldMonth
     * @param oldDay
     */
    private void settingDateRamos(int newYear, int newMonth, int newDay, int oldYear, int oldMonth, int oldDay) {
        if (newYear > oldYear) {
            for (int i = 0; i < newYear - oldYear; i++) {
                Utils.tapScreenXY("180 1560");
                Utils.sleep(2000);
            }
        } else if (newYear < oldYear) {
            for (int i = 0; i < oldYear - newYear; i++) {
                Utils.tapScreenXY("200 1420");
                Utils.sleep(2000);
            }
        }

        if (newMonth > oldMonth) {
            for (int i = 0; i < newMonth - oldMonth; i++) {
                Utils.tapScreenXY("590 1560");
                Utils.sleep(2000);
            }
        } else if (oldMonth > newMonth) {
            for (int i = 0; i < oldMonth - newMonth; i++) {
                Utils.tapScreenXY("590 1425");
                Utils.sleep(2000);
            }
        }

        if (newDay > oldDay) {
            for (int i = 0; i < newDay - oldDay; i++) {
                Utils.tapScreenXY("920 1564");
                Utils.sleep(2000);
            }
        } else if (oldDay > newDay) {
            for (int i = 0; i < oldDay - newDay; i++) {
                Utils.tapScreenXY("920 1425");
                Utils.sleep(2000);
            }
        }
    }

    /***
     *
     * 設置性別
     */
    private void settingSex(UserInfoEntity infoEntity) {
        LogUtils.logInfo("設置性別");
        AccessibilityNodeInfo sexNodeInfo = Utils.findViewByText(service, "性别");
        if (sexNodeInfo != null) {
            Utils.clickCompone(sexNodeInfo);
            Utils.sleep(3000);
            AccessibilityNodeInfo sexinfo = Utils.findViewById(service, "com.tencent.mobileqq:id/action_sheet_actionView");
            if (sexinfo != null) {
                String phonetype = Utils.getSystemModel();
                switch (phonetype) {
                    case Constants.HONOR_PHONE_MODEL:
                        if (infoEntity.getSex().equals("男")) {
                            for (int i = 0; i < 2; i++) {
                                Utils.tapScreenXY("333 964");
                                Utils.sleep(1000);
                            }
                        } else {
                            for (int i = 0; i < 2; i++) {
                                Utils.tapScreenXY("333 1074");
                                Utils.sleep(1000);
                            }
                        }
                        break;
                    case Constants.RAMOS_PHONE_MODEL:
                        if (infoEntity.getSex().equals("男")) {
                            for (int i = 0; i < 2; i++) {
                                Utils.tapScreenXY("530 1420");
                                Utils.sleep(1000);
                            }
                        } else {
                            for (int i = 0; i < 2; i++) {
                                Utils.tapScreenXY("510 1570");
                                Utils.sleep(1000);
                            }
                        }
                        break;
                }
                Utils.clickCompone(Utils.findViewByText(service, "完成"));
                Utils.sleep(1500);
            }
        } else {
            LogUtils.logInfo("未找到标识");
        }
    }

    /***
     *
     * 設置昵稱
     */
    private void modifyNickName(String nickName) {
        LogUtils.logInfo("設置昵稱");
        AccessibilityNodeInfo editinfo = Utils.findViewByType(service, EditText.class.getName());
        if (editinfo != null) {
            Utils.componeFocus(editinfo);
            Utils.sleep(1000);
            Utils.selectAllText(editinfo);
            Utils.sleep(1000);
            Utils.inputText(service, editinfo, nickName);
            Utils.sleep(2000);
        }
    }

    /***
     * 修改签名
     */
    private void ModifyAutograph(String sign) {
        LogUtils.logInfo("修改签名  " + sign);
        if (!TextUtils.isEmpty(sign)) {
            AccessibilityNodeInfo signature = Utils.findViewByText(service, "签名");
            if (signature != null) {
                String signtxt = (String) signature.getParent().getChild(2).getChild(1).getText();
                String signText = "";
                if (!TextUtils.isEmpty(signtxt)) {
                    signText = signtxt.toString();
                }

                LogUtils.logError("   签名  = " + signText);
                if (!sign.equals(signText) && TextUtils.isEmpty(signText)) {
                    Utils.clickCompone(signature);
                    Utils.sleep(5000);

                    AccessibilityNodeInfo info = Utils.findViewByText(service, "写签名");
                    if (info != null) {
                        Utils.clickCompone(info);
                        Utils.sleep(4000);
                        inputText(sign);
                    } else {
                        AccessibilityNodeInfo et_text = Utils.findViewByType(service, EditText.class.getName());
                        if (et_text == null) {
                            return;
                        }
                        if (!TextUtils.isEmpty(sign)) {
                            Utils.componeFocus(et_text);
                            Utils.sleep(1000);
                            Utils.selectAllText(et_text);
                            Utils.sleep(1000);
                            Utils.inputText(service, et_text, sign);
                            Utils.sleep(4000);
                        }
                        Utils.clickCompone(Utils.findViewByText(service, "发布签名"));
                        Utils.sleep(5000);
                    }
                }
                //com.qqindividuality:id/status_commit_button    发布签名
            }
        }
    }

    /**
     * 选择相册
     */
    private void settingHeadPortrait(String filepath) {
        if (!TextUtils.isEmpty(filepath)) {
            if (Utils.findViewByText(service, "头像") != null) {
                Utils.clickCompone(Utils.findViewByText(service, "头像"));
                Utils.sleep(1500);

                Utils.clickCompone(Utils.findViewByText(service, "从相册选择图片"));
                Utils.sleep(5000);

                AccessibilityNodeInfo photos = Utils.findViewById(service, "com.tencent.mobileqq:id/ivTitleBtnLeft");
                if (photos != null) {
                    LogUtils.logInfo(" 找到相冊 ");
//            Utils.clickCompone(photos);// 相册  com.tencent.mobileqq:id/ivTitleBtnLeft
                    Utils.clickComponeByXY(photos);
                    Utils.sleep(5000);

                    AccessibilityNodeInfo camera = Utils.findViewByText(service, "Camera");
                    AccessibilityNodeInfo photo = Utils.findViewByText(service, "QQPhoto");
                    if (camera != null) {
                        Utils.clickComponeByXY(camera);
                        Utils.sleep(4000);
                        settingHeard(filepath);
                    } else if (photo != null) {
                        Utils.clickComponeByXY(photo);
                        Utils.sleep(4000);
                        settingHeard(filepath);
                    } else {
                        Utils.clickCompone(Utils.findViewByText(service, "取消"));//com.tencent.mobileqq:id/ivTitleBtnRightText
                        Utils.sleep(2000);
                    }
                }
            }
        }
    }

    private void returnBack(String data) {
        Utils.delImageToPhoto();
        FileUtils.deleteFile(data);
        boolean isfind = true;
        while (isfind) {
            AccessibilityNodeInfo nodeInfo = Utils.findViewByDesc(service, "帐户及设置");
            if (nodeInfo != null) {
                isfind = false;
            } else {
                Utils.pressBack(service);
                Utils.sleep(2000);
            }
        }
        LogUtils.logInfo("修改信息執行完成");
        handler.sendEmptyMessage(1);
    }

    /***
     * 设置头像
     * @param images
     */
    private void settingHeard(String images) {
        AccessibilityNodeInfo imges = Utils.findViewById(service, "com.tencent.mobileqq:id/photo_list_gv");
        if (imges != null && imges.getChildCount() > 0) {
            LogUtils.logInfo("size = " + imges.getChildCount() + "    ,   ");
            Utils.clickComponeByXY(imges.getChild(0));
            Utils.sleep(4000);
        }

        Utils.clickCompone(Utils.findViewByText(service, "完成"));
        Utils.sleep(1500);

        Utils.delImageToPhoto();
        FileUtils.deleteFile(images);
        Utils.sleep(3000);

        Utils.clickCompone(Utils.findViewByText(service, "返回"));
        Utils.sleep(1500);
    }

    /**
     * Edittext 输入
     *
     * @param str
     */
    private void inputText(String str) {
        LogUtils.logInfo("编辑签名");
        AccessibilityNodeInfo edit = Utils.findViewByText(service, "编辑签名");
        if (edit != null) {
            AccessibilityNodeInfo et_text = Utils.findViewByType(service, EditText.class.getName());
            if (et_text == null) {
                return;
            }
            LogUtils.logInfo("   " + et_text.getText());
            LogUtils.logInfo("new 簽名=" + str);
            Utils.sleep(2000);
            if (str.equals(et_text.getText().toString())) {
                LogUtils.logInfo("  簽名一致 ");
                AccessibilityNodeInfo backInfo = Utils.findViewByText(service, "返回");
                if (backInfo != null) {
                    Utils.clickComponeByXY(backInfo);//返回
                    Utils.sleep(3000);
                }

                AccessibilityNodeInfo dialog = Utils.findViewById(service, "android:id/content");
                if (dialog != null) {
                    Utils.clickComponeByXY(Utils.findViewByDesc(service, "退出按钮"));//com.tencent.mobileqq:id/dialogLeftBtn
                    Utils.sleep(2500);
                }
            } else {
                if (!TextUtils.isEmpty(str)) {
                    Utils.componeFocus(et_text);
                    Utils.sleep(1000);
                    Utils.selectAllText(et_text);
                    Utils.sleep(1000);
                    Utils.inputText(service, et_text, str);
                    Utils.sleep(4000);
                }
                Utils.clickCompone(Utils.findViewByText(service, "发布签名"));
                Utils.sleep(5000);
            }
        }
    }

    /***
     *
     * 下载图片
     * */
    public void downloadPicture(String url) {
        if (url != null) {
            HttpTask.getInstance().download(url, new HttpTask.HttpCallback() {
                @Override
                public void onSuccess(String data) {
                    LogUtils.logInfo("    data    " + data);
                    if (!TextUtils.isEmpty(data)) {
                        File fileName = new File(data);
//                        LogUtils.logInfo("    fileName    " + fileName.toString());
                        Utils.saveImageToPhoto(service, fileName);
                        settingHeadPortrait(data);
                        returnBack(data);
                    } else {
                        LogUtils.logInfo("未下载图片");
                        returnBack(data);
                    }
                }

                @Override
                public void onFailure(String errMsg) {
                    LogUtils.logInfo("头像下载失败  " + errMsg);
                    returnBack("");
                }

                @Override
                public void onFinished() {

                }
            });
        } else {
            LogUtils.logInfo("url 为空 无值");
            returnBack("");
        }
    }
}
