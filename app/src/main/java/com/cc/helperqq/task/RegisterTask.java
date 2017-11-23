package com.cc.helperqq.task;

import android.content.ContentValues;
import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import com.cc.helperqq.entity.GroupInfo;
import com.cc.helperqq.entity.TaskEntry;
import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.Utils;
import com.cc.helperqq.service.HelperQQService;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * 注册
 * Created by fangying on 2017/10/10.
 */

public class RegisterTask {

    private HelperQQService service;
    private Handler handler;

    int index = 0;

    public RegisterTask(HelperQQService service, Handler handler) {
        this.service = service;
        this.handler = handler;
    }

    public void add(TaskEntry taskEntry) {
        if( !Utils.isTragetActivity(Constants.QQ_HOME_ACTIVITY)){
            Utils.startPage(Constants.QQ_HOME_ACTIVITY);
            Utils.sleep(5000L);
        }

        AccessibilityNodeInfo contacts = Utils.findViewByTextMatch(service, "联系人");
        if (contacts == null) {
            handler.sendEmptyMessage(1);
            return;
        }
        Utils.clickCompone(contacts);
        Utils.sleep(2000);

        Utils.clickCompone(Utils.findViewByTextMatch(service, "群"));
        Utils.sleep(5000);

        List<AccessibilityNodeInfo> listText = Utils.findViewListById(service, "com.tencent.mobileqq:id/text1");
        if (listText != null && listText.size() > 0) {
            if (index < listText.size()) {
                List<GroupInfo> groupInfos = DataSupport.where("wxsign= ? and groupType = ?", taskEntry.getWx_sign(), "success").find(GroupInfo.class);
                if (groupInfos.size() < listText.size()) {
                    String groupName = listText.get(index).getText() + "";
                    Utils.clickCompone(Utils.findViewByText(service, groupName));
                    Utils.sleep(5000);

                    AccessibilityNodeInfo groupNotice = Utils.findViewByText(service, "群公告");
                    if (groupNotice != null) {
                        Utils.clickCompone(Utils.findViewByText(service, "我知道了"));
                        Utils.sleep(3000);
                    }
                    // com.tencent.mobileqq:id/dialogText
                    AccessibilityNodeInfo info = Utils.findViewByText(service, "该群因涉及违反相关条例，已被永久封停，不能使用。系统将会自动解散该群。");
                    if (info != null) {
                        // com.tencent.mobileqq:id/dialogRightBtn
                        Utils.clickCompone(Utils.findViewByText(service, "我知道了"));
                        Utils.sleep(3000L);
                        index++;
                        add(taskEntry);
                    }

                    AccessibilityNodeInfo groupChat = Utils.findViewByDesc(service, "群资料卡");
                    if (groupChat != null) {
                        Utils.clickCompone(groupChat);
                        Utils.sleep(4000L);
                        List<AccessibilityNodeInfo> list = Utils.findViewListByType(service, TextView.class.getName());
                        String groupId = list.get(1).getText().toString();

                        List<GroupInfo> groupInfoList = DataSupport.where("wxsign= ? and groupId = ?", taskEntry.getWx_sign(), groupId).find(GroupInfo.class);
                        if (groupInfoList.size() == 0) {
                            GroupInfo groupInfo = new GroupInfo();
                            groupInfo.setWxsign(taskEntry.getWx_sign());
                            groupInfo.setGroupName(Utils.getBASE64(groupName));
                            groupInfo.setGroupId(groupId);
                            groupInfo.setGroupType("success");
                            groupInfo.saveThrows();
                        } else {
                            int id = groupInfoList.get(0).getId();
                            ContentValues values = new ContentValues();
                            values.put("groupType", "success");
                            DataSupport.update(GroupInfo.class, values, id);
                        }

                        Utils.pressBack(service);
                        Utils.sleep(3000);

                        Utils.pressBack(service);
                        Utils.sleep(3000);

                        index++;
                        add(taskEntry);
                    }
                } else {
                    if (index < listText.size()) {
                        String groupName = listText.get(index).getText() + "";
                        List<GroupInfo> groupInfos1 = DataSupport.where("wxsign= ? and groupType =? ",
                                taskEntry.getWx_sign(), "success").find(GroupInfo.class);

                        if (groupInfos1.size() == 0) {
                            Utils.clickCompone(Utils.findViewByText(service, groupName));
                            Utils.sleep(3000);

                            AccessibilityNodeInfo groupNotice = Utils.findViewByText(service, "群公告");
                            if (groupNotice != null) {
                                Utils.clickCompone(Utils.findViewByText(service, "我知道了"));
                                Utils.sleep(3000);
                            }
                            // com.tencent.mobileqq:id/dialogText
                            AccessibilityNodeInfo info = Utils.findViewByText(service, "该群因涉及违反相关条例，已被永久封停，不能使用。系统将会自动解散该群。");
                            if (info != null) {
                                // com.tencent.mobileqq:id/dialogRightBtn
                                Utils.clickCompone(Utils.findViewByText(service, "我知道了"));
                                Utils.sleep(3000L);
                                index++;
                                add(taskEntry);
                            }

                            AccessibilityNodeInfo groupChat = Utils.findViewByDesc(service, "群资料卡");
                            if (groupChat != null) {
                                Utils.clickCompone(groupChat);
                                Utils.sleep(4000L);
                                List<AccessibilityNodeInfo> list = Utils.findViewListByType(service, TextView.class.getName());
                                String groupId = list.get(1).getText().toString();

                                GroupInfo groupInfo = new GroupInfo();
                                groupInfo.setWxsign(taskEntry.getWx_sign());
                                groupInfo.setGroupName(Utils.getBASE64(groupName));
                                groupInfo.setGroupId(groupId);
                                groupInfo.setGroupType("success");
                                groupInfo.saveThrows();

                                Utils.pressBack(service);
                                Utils.sleep(3000);

                                Utils.pressBack(service);
                                Utils.sleep(3000);

                                index++;
                                add(taskEntry);
                            }
                        } else {
                            index++;
                            add(taskEntry);
                        }
                    }
                }
            } else {
                handler.sendEmptyMessage(1);
            }
        } else {
            handler.sendEmptyMessage(1);
        }
    }
//    public void register() {
//        AccessibilityNodeInfo lon = Utils.findViewById(service, "android:id/content");
//        if (lon != null) {
//            Utils.clickCompone(Utils.findViewByText(service, "允许"));//com.huawei.systemmanager:id/btn_allow
//            Utils.sleep(2000);
//        }
//
//        AccessibilityNodeInfo newUser = Utils.findViewByText(service, "新用户");//com.tencent.mobileqq:id/btn_register
//        if (newUser != null) {
//            Utils.clickCompone(newUser);
//            Utils.sleep(2000);
//        }
//
//        if (Utils.isTragetActivity(Constants.QQ_REGISTER_ACTIVITY)) {
//            AccessibilityNodeInfo editNum = Utils.findViewByText(service, "输入手机号码");
//            if (editNum != null) {
//                AccessibilityNodeInfo edittxt = Utils.findViewByType(service, EditText.class.getName());
//                if (edittxt != null) {
//                    Utils.inputText(service, edittxt, "");
//                    Utils.inputText("");
//                    Utils.sleep(3000);
//                }
//                AccessibilityNodeInfo nextstep_btn = Utils.findViewByText(service, "下一步");
//                if (nextstep_btn != null && nextstep_btn.isEnabled()) {
//                    Utils.clickCompone(nextstep_btn);
//                    Utils.sleep(2000);
//                }
//            }
//
//            boolean isfind = true;
//            int index = 0;
//            while (isfind) {
//                AccessibilityNodeInfo progressBarNodeInfo = Utils.findViewByType(service, ProgressBar.class.getName());
//                if (progressBarNodeInfo != null) {
//                    //com.tencent.mobileqq:id/dialogText   正在发送短信
//                    Utils.sleep(4000);
//                    index++;
//                } else {
//                    isfind = false;
//                }
//                if (index == 4) {
//                    Utils.pressBack(service);
//                    Utils.sleep(2000);
//                    isfind = false;
//                }
//            }
//
//        }
//    }
}
