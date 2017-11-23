package com.cc.helperqq.task;

import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.cc.helperqq.entity.GroupInfo;
import com.cc.helperqq.entity.GroupMembersInfo;
import com.cc.helperqq.entity.TaskEntry;
import com.cc.helperqq.service.HelperQQService;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.Utils;

import org.litepal.crud.DataSupport;

import java.util.List;


/**
 * 创建讨论组
 * Created by fangying on 2017/11/13.
 */

public class CreateMultiplayerChatTask {

    private Handler handler;
    private HelperQQService service;
    private int groupNum = 0;  // 第几的个群
    private int memberIndex = 0;  // 成员
    private int mpcMemberIndex = 0;// 讨论组成员数目

    public CreateMultiplayerChatTask(HelperQQService service, android.os.Handler handler) {
        this.service = service;
        this.handler = handler;
    }

    public void CreateChat(TaskEntry taskEntry) {
        AccessibilityNodeInfo contacts = Utils.findViewByTextMatch(service, "联系人");
        if (contacts == null) {
            handler.sendEmptyMessage(1);
            return;
        }
        Utils.clickCompone(contacts);
        Utils.sleep(2000);

        Utils.clickCompone(Utils.findViewByTextMatch(service, "多人聊天"));
        Utils.sleep(5000);

        AccessibilityNodeInfo createChat = Utils.findViewByText(service, "创建多人聊天");
        if (createChat != null) {
            Utils.clickCompone(createChat);
            Utils.sleep(3000L);
        }
        SelectionCategoryCreationMulitiChat(taskEntry);
    }

    /***
     * 选择类别创建讨论组
     * @param taskEntry
     */
    private void SelectionCategoryCreationMulitiChat(TaskEntry taskEntry) {
        String wxsign = taskEntry.getWx_sign();
        List<GroupInfo> groupInfoList = DataSupport.where("wxsign = ? and groupType = ?", wxsign, "success").find(GroupInfo.class);

        // 3502079885  1413869836  2161535928  2125509598 1308996636 3267195686
        int type = 1;
        AccessibilityNodeInfo groupChat;
        switch (type) {
            case 0:
                // 面对面发起多人聊天
                groupChat = Utils.findViewByTextMatch(service, "面对面发起多人聊天");
                if (groupChat != null) {
                    Utils.clickComponeByXY(groupChat);
                    Utils.sleep(3000L);
                }
                break;

            case 1:
                // 从群聊中选择
                groupChat = Utils.findViewByTextMatch(service, "从群聊中选择");
                if (groupChat != null) {
                    Utils.clickComponeByXY(groupChat);
                    Utils.sleep(3000L);

                    AccessibilityNodeInfo edit = Utils.findViewByType(service, EditText.class.getName());
                    if (edit != null) {
                        Utils.clickComponeByXY(edit);
                        Utils.sleep(2000L);
                    }
                    inputTxtGroupId(groupInfoList, wxsign);
                }
                break;

            case 2:
                // 选择分类创建
                groupChat = Utils.findViewByTextMatch(service, "选择分类创建群");
                if (groupChat != null) {
                    Utils.clickComponeByXY(groupChat);
                    Utils.sleep(3000L);
                }
                break;

            case 3:
                // 好友列表中选择
                groupChat = Utils.findViewByTextMatch(service, "我的好友");
                if (groupChat != null) {
                    Utils.clickComponeByXY(groupChat);
                    Utils.sleep(3000L);
                }
                break;
        }
    }

    /**
     * 查找输入群
     *
     * @param groupInfoList 群集合
     */
    private void inputTxtGroupId(List<GroupInfo> groupInfoList, String wxsign) {
        AccessibilityNodeInfo ivtitleName = Utils.findViewById(service, "com.tencent.mobileqq:id/ivTitleName");
        String titleName = ivtitleName.getText() + "";
        if (titleName.equals("选择群聊")) {
            if (groupNum < groupInfoList.size()) {
                String groupId = groupInfoList.get(groupNum).getGroupId();
                AccessibilityNodeInfo editText = Utils.findViewByType(service, EditText.class.getName());
                if (editText != null) {
                    if (editText.getText() != null) {
                        Utils.inputText(service, editText, groupId);
                    } else {
                        Utils.inputText(groupId);
                    }
                    Utils.sleep(3000);

                    // com.tencent.mobileqq:id/title   txt 诚信点赞
                    AccessibilityNodeInfo result = Utils.findViewByText(service, "相关的本地结果");
                    if (result == null) {
                        String groupName = groupInfoList.get(groupNum).getGroupName();
                        LogUtils.logInfo("groupName =" + groupName + "  " + Utils.getFromBASE64(groupName) + "groupId =" + groupId);
                        // com.tencent.mobileqq:id/title
                        AccessibilityNodeInfo title = Utils.findViewById(service, "com.tencent.mobileqq:id/title");
                        if (title != null) {
                            String group_name = title.getText() + "";
                            LogUtils.logInfo("点击群");
                            Utils.clickCompone(title);
                            Utils.sleep(3000L);

                            boolean isprogBar = true;
                            while (isprogBar) {
                                AccessibilityNodeInfo pb = Utils.findViewByType(service, ProgressBar.class.getName());
                                if (pb != null) {
                                    Utils.sleep(3000L);
                                } else {
                                    isprogBar = false;
                                }
                            }
                            List<GroupMembersInfo> groupMembersInfos = DataSupport.where("wxsign = ? and groupId = ?",
                                    wxsign, groupId).find(GroupMembersInfo.class);
                            inputTextMemberId(groupId, group_name, groupInfoList,groupMembersInfos, wxsign);
                        }
                    } else {
                        groupNum++;
                        inputTxtGroupId(groupInfoList, wxsign);
                    }
                }
            } else {
                handler.sendEmptyMessage(1);
            }
        }
    }

    private void inputTextMemberId(String groupId, String groupName, List<GroupInfo> groupInfoList,List<GroupMembersInfo> groupMembersInfos, String wxsign) {
        LogUtils.logInfo(" groupName = " + groupName);
        AccessibilityNodeInfo title = Utils.findViewById(service, "com.tencent.mobileqq:id/ivTitleName");
        String group_name = title.getText() + "";
        LogUtils.logInfo(" group_name = " + group_name);
        if (group_name.equals(groupName)) {
            if (mpcMemberIndex < 20) {
                if (memberIndex < groupMembersInfos.size()) {
                    String membersId = groupMembersInfos.get(memberIndex).getMemberId();
                    LogUtils.logInfo(" membersId = " + membersId);
                    AccessibilityNodeInfo mpcEdit = Utils.findViewByType(service, EditText.class.getName());
                    if (mpcEdit != null) {
                        Utils.inputText(service, mpcEdit, membersId);
                        Utils.sleep(3000L);
                        AccessibilityNodeInfo memResult = Utils.findViewByText(service, "相关的本地结果");
                        if (memResult == null) {
                            AccessibilityNodeInfo groupMember = Utils.findViewByText(service, "群成员");
                            if (groupMember != null) {
                                Utils.clickCompone(groupMember);
                                Utils.sleep(3000L);

                                AccessibilityNodeInfo checkBoxinfo = Utils.findViewByType(service, CheckBox.class.getName());
                                if (checkBoxinfo != null) {
                                    memberIndex++;
                                    mpcMemberIndex++;
                                    inputTextMemberId(groupId, groupName, groupInfoList,groupMembersInfos, wxsign);
                                } else {
                                    groupNum++;
                                    // 切换下一群组
                                    // com.tencent.mobileqq:id/ivTitleBtnLeft
//                                    AccessibilityNodeInfo group = Utils.findViewByTextMatch(service, "群");
//                                    if (group != null) {
//                                        Utils.clickComponeByXY(group);
//                                        Utils.sleep(3000L);
                                    Utils.pressBack(service);
                                    Utils.sleep(3000L);
                                    Utils.pressBack(service);
                                    Utils.sleep(3000L);
                                    inputTxtGroupId(groupInfoList, wxsign);
//                                    }
                                }
                            }
                        } else {
                            memberIndex++;
                            inputTextMemberId(groupId, groupName, groupInfoList,groupMembersInfos, wxsign);
                        }
                    }
                } else {
                    groupNum++;
                    // 切换下一群组
                    // com.tencent.mobileqq:id/ivTitleBtnLeft
                    AccessibilityNodeInfo group = Utils.findViewByTextMatch(service, "群");
                    if (group != null) {
                        Utils.clickComponeByXY(group);
                        Utils.sleep(3000L);
                        inputTxtGroupId(groupInfoList, wxsign);
                    }
                }
            } else {
                AccessibilityNodeInfo completeBtn = Utils.findViewByText(service, "完成");
                if (completeBtn != null && completeBtn.isEnabled()) {
                    Utils.clickComponeByXY(completeBtn);
                    Utils.sleep(3000L);

                    AccessibilityNodeInfo actionView = Utils.findViewById(service, "com.tencent.mobileqq:id/action_sheet_actionView");
                    if (actionView != null) {
                        int mpctype = 0;
                        switch (mpctype) {
                            case 0:
                                // 创建多人聊天
                                AccessibilityNodeInfo mpc = Utils.findViewByText(service, "创建多人聊天");
                                if (mpc != null) {
                                    Utils.clickComponeByXY(mpc);
                                    Utils.sleep(3000);
                                }
                                break;

                            case 1:
                                // 创建群
                                AccessibilityNodeInfo mpg = Utils.findViewByText(service, "创建群");
                                if (mpg != null) {
                                    Utils.clickComponeByXY(mpg);
                                    Utils.sleep(3000);
                                }
                                break;
                        }

                        Utils.pressBack(service);
                        Utils.sleep(3000L);
                        handler.sendEmptyMessage(1);
                    }
                }
            }
        }
    }
}
