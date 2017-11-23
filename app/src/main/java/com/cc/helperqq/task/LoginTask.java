package com.cc.helperqq.task;

import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import com.cc.helperqq.entity.TaskEntry;
import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.ShellUtils;
import com.cc.helperqq.utils.Utils;
import com.cc.helperqq.service.HelperQQService;

import java.util.List;


public class LoginTask {

    /**
     * 手机
     */
    public static final String PAGE_MOBILE_LOGIN = "com.tencent.mm/.ui.account.mobile.MobileInputUI";

    /**
     * 微信号，邮箱
     */
    private static final String PAGE_OTHER_LOGIN = "com.tencent.mm/.ui.account.LoginUI";


    public static void login(HelperQQService service, TaskEntry entry) {
        LogUtils.logInfo("   login     重新登陆  QQ    ");
        if (!TextUtils.isEmpty(entry.getWx_id())) {
            telLogin(service, entry);
        }
        Utils.sleep(20 * 1000L);
    }

    public static void startMobileLogin() {
        ShellUtils.execCommand("am start -n " + PAGE_MOBILE_LOGIN + " --ei mobile_input_purpose 5", true);
    }

    public static void accountLogin(HelperQQService service, TaskEntry taskEntry) {
        List<AccessibilityNodeInfo> editors = Utils.findViewListByType(service, EditText.class.getName());
        if (editors != null && editors.size() > 1) {
            String account = taskEntry.getWx_id();
            AccessibilityNodeInfo firstPwd = editors.get(0);
            AccessibilityNodeInfo secondPwd = editors.get(1);
            Utils.inputText(service, firstPwd, account);
            Utils.sleep(3 * 1000L);
            Utils.inputText(service, secondPwd, taskEntry.getWx_pass());
            Utils.sleep(3 * 1000L);
            Utils.clickCompone(Utils.findViewByText(service, Button.class.getName(), "登录"));
        }
    }

//    public static void emailLogin(HeplerQQService service, TaskEntry taskEntry) {
//        List<AccessibilityNodeInfo> editors = Utils.findViewListByType(service, EditText.class.getName());
//        if (editors != null && editors.size() > 1) {
//            AccessibilityNodeInfo firstPwd = editors.get(0);
//            AccessibilityNodeInfo secondPwd = editors.get(1);
//            Utils.inputText(service, firstPwd, taskEntry.getWx_email());
//            Utils.sleep(3 * 1000L);
//            Utils.inputText(service, secondPwd, taskEntry.getWx_pass());
//            Utils.sleep(3 * 1000L);
//            Utils.clickCompone(Utils.findViewByText(service, Button.class.getName(), "登录"));
//        }
//    }

    public static void telLogin(HelperQQService service, TaskEntry taskEntry) {
        LogUtils.logError("   进入登陆界面     输入帐密");
        List<AccessibilityNodeInfo> editors = Utils.findViewListByType(service, EditText.class.getName());
        if (editors != null && editors.size() > 1) {

            Utils.inputText(service, Utils.findViewByDesc(service, Constants.QQ_LOGIN_ACCOUNT), taskEntry.getWx_id());
            Utils.sleep(3 * 1000L);

            Utils.inputText(service, Utils.findViewById(service, Constants.QQ_LOGIN_PASSWORD), taskEntry.getWx_pass());
            Utils.sleep(3 * 1000L);

            Utils.clickCompone(Utils.findViewByText(service, Button.class.getName(), Constants.QQ_LOGIN_BTN));
            loginAndVerification(service);
        }
    }


//    public static void telLogin(HeplerQQService service, AccountInfo accountInfo) {
//        List<AccessibilityNodeInfo> editors = Utils.findViewListByType(service, EditText.class.getName());
//        if (editors != null && editors.size() > 1) {
//            AccessibilityNodeInfo firstPwd = editors.get(1);
//            AccessibilityNodeInfo secondPwd = editors.get(2);
//            Utils.inputText(service, firstPwd, accountInfo.getWx_id());
//            Utils.sleep(3 * 1000L);
//            Utils.inputText(service, secondPwd, accountInfo.getWx_pass());
//            Utils.sleep(3 * 1000L);
//            Utils.clickCompone(Utils.findViewByText(service, Button.class.getName(), "登录"));
//        }
//    }

    public static void loginQQ(HelperQQService service) {
        LogUtils.logInfo("   判断界面  ");
        Utils.sleep(20 * 1000L);
        AccessibilityNodeInfo loginBtn;
        AccessibilityNodeInfo clear = Utils.findViewByText(service, "跳过");
        if (clear != null) {
            Utils.clickCompone(clear);
            Utils.sleep(3000);
        }
        if (Utils.findViewByTextMatch(service, "新用户") != null) {
            if ((loginBtn = Utils.findViewById(service, "com.tencent.mobileqq:id/btn_login")) != null) {
                Utils.clickCompone(loginBtn);
                Utils.sleep(2 * 1000L);
            }
        }
    }

    private static void loginAndVerification(HelperQQService service) {
        boolean isfind = true;
        while (isfind) {
            AccessibilityNodeInfo logining = Utils.findViewByText(service, "登录中");
            if (logining != null) {
                Utils.sleep(3000);
            } else {
                isfind = false;
            }
        }

        Utils.sleep(7000);
        AccessibilityNodeInfo verification = Utils.findViewById(service, "com.tencent.mobileqq:id/ivTitleName");
        if (verification != null && !TextUtils.isEmpty(verification.getText())) {
            String title = verification.getText() + "";
            if (title.equals("验证码")) {
                AccessibilityNodeInfo sur = Utils.findViewByType(service, WebView.class.getName());
                if (sur != null) {
                    LogUtils.logError("   亲爱的用户，我们只想确认你不是机器人。   ");
                    String typePhone = Utils.getSystemModel();
                    switch (typePhone) {
                        case Constants.HONOR_PHONE_MODEL:
                            Utils.sleep(1000);
                            LogUtils.logInfo(" 华为----点击验证码输入框 ");
                            Utils.tapScreenXY("420 440");
                            LogUtils.logInfo(" 等待输入验证码 ");
                            Utils.sleep(30000);
                            LogUtils.logInfo(" 等待输入验证码    1   ");

                            Utils.tapScreenXY("354 700");
                            Utils.sleep(2000);
                            break;

                        case Constants.RAMOS_PHONE_MODEL:
                            LogUtils.logInfo(" 蓝魔----点击验证码输入框 ");
                            Utils.sleep(1000);
                            Utils.tapScreenXY("583 630");
                            LogUtils.logInfo(" 等待输入验证码 ");
                            Utils.sleep(30000);
                            LogUtils.logInfo(" 等待输入验证码    1   ");

                            Utils.tapScreenXY("537 1050");
                            Utils.sleep(2000);
                            break;
                    }
                }
            }
        }

        isfind = true;
        while (isfind) {
            AccessibilityNodeInfo logining = Utils.findViewByText(service, "登录中");
            if (logining != null) {
                Utils.sleep(3000);
            } else {
                isfind = false;
            }
        }
        Utils.sleep(5000);
    }

}
