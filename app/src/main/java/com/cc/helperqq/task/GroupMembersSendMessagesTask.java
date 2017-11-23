package com.cc.helperqq.task;

import android.content.ContentValues;
import android.os.Handler;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cc.helperqq.entity.GroupInfo;
import com.cc.helperqq.entity.MembersInfo;
import com.cc.helperqq.entity.MessageEntity;
import com.cc.helperqq.entity.TaskEntry;
import com.cc.helperqq.http.HttpHandler;
import com.cc.helperqq.http.HttpTask;
import com.cc.helperqq.service.HelperQQService;
import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.FileUtils;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.Utils;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by fangying on 2017/11/6.
 */

public class GroupMembersSendMessagesTask {

    private HelperQQService service;
    private Handler handler;
    private static final int SEND_PHOTO = 1;
    private static final int SEND_TEXT = 2;
    private int degree = 0;
    private int isNum = 1;
    private int membersNum = 0;
    private int sendNum = 0;
    private int isfrist = 0;

    public GroupMembersSendMessagesTask(HelperQQService service, Handler handler) {
        this.service = service;
        this.handler = handler;
    }

    /***
     * 获取回复数据
     * @param taskEntry
     */
    public void sendTypeMsg(final TaskEntry taskEntry) {
        if (sendNum<5){
            if (taskEntry != null) {
                LogUtils.logInfo("      请求获取回复数据       ");
                HttpHandler.getqunfaMsg(taskEntry.getWx_sign(), "", new HttpTask.HttpCallback() {
                    @Override
                    public void onSuccess(String data) {
                        LogUtils.logInfo("data  =" + data);
                        if (!TextUtils.isEmpty(data) && (data.length() > 10)) {
                            List<MessageEntity> messageEntities = new ArrayList<MessageEntity>();
                            try {
                                org.json.JSONArray jsonArray = new org.json.JSONArray(data);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    MessageEntity messageEntity = new MessageEntity();
                                    int type = jsonObject.getInt("type");
                                    String sc_id = jsonObject.getString("sc_id");
                                    String wx_sign = jsonObject.getString("wx_sign");
                                    String text = jsonObject.getString("text");
                                    String imgUrl = jsonObject.getString("imgUrl");
                                    String weburl = jsonObject.getString("weburl");

                                    messageEntity.setImgUrl(imgUrl);
                                    messageEntity.setWx_sign(wx_sign);
                                    messageEntity.setSc_id(sc_id);
                                    messageEntity.setText(text);
                                    messageEntity.setType(type);
                                    messageEntity.setWeburl(weburl);

                                    messageEntities.add(messageEntity);
                                    messageEntity = null;
                                }
                                sendGroupMember(messageEntities, taskEntry);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            HttpHandler.reportError(taskEntry.getWx_sign(), "获取回复數據为空!");
                            handler.sendEmptyMessage(1);
                        }
                    }

                    @Override
                    public void onFailure(String errMsg) {
                        LogUtils.logInfo("err  =" + errMsg);
                        HttpHandler.reportError(taskEntry.getWx_sign(), "获取回复數據失败!");
                        handler.sendEmptyMessage(1);
                    }

                    @Override
                    public void onFinished() {
                    }
                });
            }
        }
    }

    public void sendGroupMember(List<MessageEntity> messageEntities, TaskEntry taskEntry) {
        if (sendNum < 5 && membersNum >= 0) {
            // 数据库数据不对   抓取群数据对比
            List<GroupInfo> groupInfos = DataSupport.where("wxsign = ? ", taskEntry.getWx_sign()).order("groupId desc").find(GroupInfo.class);
            Utils.sleep(2000L);
            for (int i = 0; i < groupInfos.size(); i++) {
                LogUtils.logInfo("groupInfos size =" + groupInfos.size() + "  i =" + i + "  GroupId = " + groupInfos.get(i).getGroupId());
            }
            if (groupInfos.size() > 0) {
                LogUtils.logInfo(" membersNum = " + membersNum);
                groupMembers(messageEntities, taskEntry, groupInfos, membersNum);
//                groupMembers(messageEntities, taskEntry, groupInfos);
            } else {
                // 数据库没有数据执行 在群列表点击群昵称进入
                LogUtils.logInfo("数据库没有数据执行  在群列表点击群昵称进入");
                AccessibilityNodeInfo contacts = Utils.findViewByTextMatch(service, "联系人");
                if (contacts == null) {
                    handler.sendEmptyMessage(1);
                    return;
                }
                Utils.clickCompone(contacts);
                Utils.sleep(2000);

                Utils.clickCompone(Utils.findViewByTextMatch(service, "群"));
                Utils.sleep(5000);

                clickGroupName(messageEntities, taskEntry, 0);
            }
        } else {
            AccessibilityNodeInfo canleBtn = Utils.findViewByText(service, Button.class.getName(), "取消");
            if (canleBtn != null) {
                Utils.clickCompone(canleBtn);
                Utils.sleep(3000L);
            }
            handler.sendEmptyMessage(1);
        }
    }

    private void groupMembers(List<MessageEntity> messageEntities, TaskEntry taskEntry, List<GroupInfo> groupInfos, int dex) {
        if (dex < groupInfos.size()) {
            LogUtils.logInfo("membersNum =" + dex + " ***********  groupInfos  size = " + groupInfos.size() + " group Name = " + groupInfos.get(dex).getGroupName() + "  Group_Type = " + groupInfos.get(dex).getGroupType());
            GroupInfo groupInfo = groupInfos.get(dex);
            if (groupInfo.getGroupType().equals("success")) {
                LogUtils.logInfo("success :" + groupInfo.getGroupId());
                AccessibilityNodeInfo contacts = Utils.findViewByTextMatch(service, "联系人");
                if (contacts == null) {
                    handler.sendEmptyMessage(1);
                    return;
                }
                Utils.clickCompone(contacts);
                Utils.sleep(2000);

                Utils.clickCompone(Utils.findViewByTextMatch(service, "群"));
                Utils.sleep(5000);

                AccessibilityNodeInfo editSou = Utils.findViewByType(service, EditText.class.getName());
                if (editSou != null) {
                    Utils.clickComponeByXY(editSou);
                    Utils.sleep(2000);

                    // 185179045   125817813
                    AccessibilityNodeInfo search_keyword = Utils.findViewById(service, "com.tencent.mobileqq:id/et_search_keyword");
                    if (search_keyword != null) {
                        Utils.inputText(groupInfo.getGroupId());
                        Utils.sleep(6000);

                        // com.tencent.mobileqq:id/title
                        AccessibilityNodeInfo title = Utils.findViewByTextMatch(service, "来自:群");
                        if (title != null) {
                            Utils.clickComponeByXY(title);
                            Utils.sleep(3000L);
                            OpenMemberList(messageEntities, taskEntry, groupInfo.getGroupId(), groupInfo.getGroupName());
                        } else {
                            membersNum++;
                            if (Utils.findViewByDesc(service, "帐户及设置") == null) {
                                if (Utils.findViewByText(service, Button.class.getName(), "取消") != null) {
                                    Utils.clickComponeByXY(Utils.findViewByText(service, Button.class.getName(), "取消"));
                                }
                                Utils.sleep(3000L);
                            }
                            sendGroupMember(messageEntities, taskEntry);
                        }
                    }
                }
            } else {
                membersNum++;
                sendGroupMember(messageEntities, taskEntry);
            }
        } else {
            LogUtils.logInfo("群成员聊天执行完成");
            handler.sendEmptyMessage(1);
        }
    }


    private boolean isfindGroupID(String grouId, List<GroupInfo> groupInfos) {
        for (GroupInfo groupInfo : groupInfos) {
            if (groupInfo.getGroupId().equals(grouId)) {
                return true;
            }
        }
        return false;
    }

    int num = 0;// 成员数量
    int memNum = 0;  // 已发成员数

    private void clickGroupName(List<MessageEntity> messageEntities, TaskEntry taskEntry, int leng) {
        List<AccessibilityNodeInfo> listText = Utils.findViewListById(service, "com.tencent.mobileqq:id/text1");
        if (listText != null && listText.size() > 0) {
            if (Utils.findViewByTextMatch(service, listText.get(leng).getText().toString()) == null) {
                Utils.swipeUp("333 960 338 464");
                Utils.sleep(3000L);
            }

            LogUtils.logInfo(" listText1 size = " + listText.size() + "   index = " + leng + "    " + listText.get(leng).getText().toString().trim());
            Utils.clickCompone(listText.get(leng));
            Utils.sleep(5000);

            AccessibilityNodeInfo gv = Utils.findViewByType(service, GridView.class.getName());
            if (gv != null) {
                Utils.pressBack(service);
                Utils.sleep(2000);
            }

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
                membersNum++;
                sendGroupMember(messageEntities, taskEntry);
            }
            AccessibilityNodeInfo groupname = Utils.findViewById(service, "com.tencent.mobileqq:id/title");
            String groupName = "";
            if (groupname != null) {
                groupName = groupname.getText() + "";
            }
            Utils.sleep(2000);
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
//                if (!isfindGroupID(groupId,)) {
//                    List<GroupInfo> groupInfos = operator.queryAllGroup(database);
//                    int id = 0;
//                    if (groupInfos.size() > 0) {
//                        id = groupInfos.size() + 1;
//                    } else {
//                        id = id + 1;
//                    }
//                    GroupInfo groupInfo = new GroupInfo();
//                    groupInfo.setGroupId(groupId);
//                    groupInfo.setGroupName(groupName);
//                    groupInfo.setGroupType("success");
//                    groupInfo.setId(id);
//                    operator.addGroupData(database, groupInfo);
//                }
                OpenMemberList(messageEntities, taskEntry, groupId, groupName);
            }
        } else {
            LogUtils.logInfo("未能加入群");
            LogUtils.logInfo("群成员聊天完成");
            handler.sendEmptyMessage(1);
        }
    }

    private void OpenMemberList(List<MessageEntity> messageEntities, TaskEntry taskEntry, String groupId, String groupName) {
        Utils.sleep(3000L);
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
            membersNum++;
            sendGroupMember(messageEntities, taskEntry);
        }

        Utils.sleep(3000L);
        boolean isfind = true;
        int ki = 0;
        while (isfind) {
            if (!Utils.isTragetActivity(Constants.QQ_GROUP_TROOP)) {
                LogUtils.logInfo("查找群资料卡控件");
                AccessibilityNodeInfo groupData = null;
                if ((groupData = Utils.findViewByDesc(service, "群资料卡")) != null) {
                    LogUtils.logInfo("点击群资料的描述性文本");
                    Utils.clickComponeByXY(groupData);
                    Utils.sleep(3000L);
                } else if ((groupData = Utils.findViewById(service, "com.tencent.mobileqq:id/ivTitleBtnRightImage")) != null) {
                    LogUtils.logInfo("点击群资料的ID");
                    Utils.clickComponeByXY(groupData);
                    Utils.sleep(3000L);
                }
            } else {
                isfind = false;
            }
            ki++;
            Utils.sleep(2000L);
            if (ki == 4) {
                if (Utils.findViewByDesc(service, "返回消息") != null) {
                    Utils.clickComponeByXY(Utils.findViewByDesc(service, "返回消息"));
                    Utils.sleep(3000L);
                } else {
                    Utils.pressBack(service);
                    Utils.sleep(3000L);
                }
                isfind = false;
                membersNum++;
                sendGroupMember(messageEntities, taskEntry);
            }
        }


        Utils.sleep(2000L);
        LogUtils.logInfo(" 是否有 groupName =" + groupName);
        if (TextUtils.isEmpty(groupName)) {
            List<AccessibilityNodeInfo> list = Utils.findViewListByType(service, TextView.class.getName());
            if (list.size() > 1) {
                int index = 0;
                isfind = true;
                while (isfind) {
                    if (!TextUtils.isEmpty(list.get(index).getText())) {
                        if (list.get(index).getText().toString().equals(groupId)) {
                            groupName = list.get(index - 1).getText().toString();
                            LogUtils.logInfo("groupName=" + groupName);
                            isfind = false;
                        } else {
                            index++;
                        }
                        if (index == list.size()) {
                            groupName = "-";
                            isfind = false;
                        }
                    }
                }
            }
        } else if (TextUtils.isEmpty(groupId)) {
            List<AccessibilityNodeInfo> list = Utils.findViewListByType(service, TextView.class.getName());
            if (list.size() > 1) {
                int index = 0;
                isfind = true;
                while (isfind) {
                    if (!TextUtils.isEmpty(list.get(index).getText())) {
                        if (list.get(index).getText().toString().equals(groupName)) {
                            groupId = list.get(index + 1).getText().toString();
                            LogUtils.logInfo("groupId=" + groupId);
                            isfind = false;
                        } else {
                            index++;
                        }
                        if (index == list.size()) {
                            groupId = "-";
                            isfind = false;
                        }
                    }
                }
            }
        }

        LogUtils.logInfo("获取成员数量");
        AccessibilityNodeInfo memberNum = Utils.findViewByText(service, "名成员");
        if (memberNum != null) {
            String numstr = memberNum.getText() + "";
            LogUtils.logInfo("获取成员数量 numstr= " + numstr);
            num = Integer.parseInt(Utils.getNumbers(numstr));
            List<MembersInfo> membersInfos = DataSupport.where("wxsign = ? and groupId= ?", taskEntry.getWx_sign(), groupId).find(MembersInfo.class);
            if (membersInfos.size() > 0) {
                memNum = membersInfos.size();
                LogUtils.logInfo("当前群组已发送条数 memNum = " + memNum);
                if (memNum >= num) {
                    membersNum++;
                    LogUtils.logInfo("切换下一个群组");
                    Utils.pressBack(service);
                    Utils.sleep(2000);
                    AccessibilityNodeInfo ziliao = Utils.findViewByDesc(service, "群资料卡");
                    if (ziliao != null) {
                        Utils.pressBack(service);
                        Utils.sleep(2000);
                    }
                    sendGroupMember(messageEntities, taskEntry);
                } else {
                    clickMembersNum(messageEntities, taskEntry, groupId, groupName, memberNum);
                }
            } else {
                clickMembersNum(messageEntities, taskEntry, groupId, groupName, memberNum);
            }
        } else {
            LogUtils.logInfo("未找到成员数目");
        }
    }

    private void clickMembersNum(List<MessageEntity> messageEntities, TaskEntry taskEntry, String groupId, String groupName, AccessibilityNodeInfo memberNum) {
        Utils.clickCompone(memberNum);
        Utils.sleep(3000);

        boolean isLoading = true;
        while (isLoading) {
            // android.widget.ProgressBar
            AccessibilityNodeInfo progressBar = Utils.findViewByType(service, ProgressBar.class.getName());
            if (progressBar == null) {
                Utils.sleep(3000L);
                isLoading = false;
            } else {
                Utils.sleep(3000L);
            }
        }

//        List<MembersInfo> infos = operator.queryAllMembers(database);
        List<MembersInfo> infos = DataSupport.where("wxsign = ?", taskEntry.getWx_sign()).find(MembersInfo.class);
        if (infos.size() > 0) {
            List<MembersInfo> membersInfoList = new ArrayList<>();
            for (MembersInfo membersInfo : infos) {
                if (membersInfo.getGroupId().equals(groupId)) {
                    membersInfoList.add(membersInfo);
                }
            }
            LogUtils.logInfo("membersInfoList  size =" + membersInfoList.size());
            isfindTvName(messageEntities, taskEntry);
            List<AccessibilityNodeInfo> tv_nameList = Utils.findViewListById(service, "com.tencent.mobileqq:id/tv_name");
            if (tv_nameList != null && tv_nameList.size() > 0) {
                findMemberName(messageEntities, taskEntry, membersInfoList, groupName, groupId, isNum, false);
            } else {
                Utils.pressBack(service);
                Utils.sleep(3000L);
                Utils.pressBack(service);
                Utils.sleep(3000L);
                Utils.pressBack(service);
                Utils.sleep(3000L);
                membersNum++;
                sendGroupMember(messageEntities, taskEntry);
            }
        } else {
            LogUtils.logInfo("数据为空");
            isfindTvName(messageEntities, taskEntry);
            findMemberName(messageEntities, taskEntry, null, groupName, groupId, isNum, false);
        }
    }

    private void isfindTvName(List<MessageEntity> messageEntities, TaskEntry taskEntry) {
        boolean isfind = true;
        int io = 0;
        while (isfind) {
            List<AccessibilityNodeInfo> tv_nameList = Utils.findViewListById(service, "com.tencent.mobileqq:id/tv_name");
            if (tv_nameList != null && tv_nameList.size() > 0) {
                Utils.sleep(3000);
                isfind = false;
            }
            LogUtils.logInfo("等待com.tencent.mobileqq:id/tv_name");
            Utils.sleep(3000L);
            io++;
            if (io > 4) {
                Utils.pressBack(service);
                Utils.sleep(3000);
                Utils.pressBack(service);
                Utils.sleep(3000);
                AccessibilityNodeInfo ziliao = Utils.findViewByDesc(service, "群资料卡");
                if (ziliao != null) {
                    Utils.pressBack(service);
                    Utils.sleep(2000);
                }
                membersNum++;
                sendGroupMember(messageEntities, taskEntry);
                isfind = false;
            }
        }
    }

    private void findMemberName(List<MessageEntity> messageEntities, TaskEntry taskEntry, List<MembersInfo> membersInfoList, String groupName, String groupId, int index, boolean isadmin) {
        isfindTvName(messageEntities, taskEntry);
        List<AccessibilityNodeInfo> tv_nameList = Utils.findViewListById(service, "com.tencent.mobileqq:id/tv_name");
        int admNum = 0;
        int surplusNum = 0;
        LogUtils.logInfo("获取成员");
        Utils.sleep(3000L);
        if (tv_nameList != null && tv_nameList.size() > 0) {
            // 是否第一次进入 即有群组管理员
            LogUtils.logInfo("是否第一次进入 即有群组管理员");
            if (isfrist == 0) {
                LogUtils.logInfo("  tv_nameList  size = " + tv_nameList.size());
                AccessibilityNodeInfo info = Utils.findViewByText(service, "群主、管理员");
                Utils.sleep(3000L);
                if (info != null) {
                    if ((!TextUtils.isEmpty(info.getText())) && info.getText().toString().contains("人")) {
                        String str = Utils.getNumbers(info.getText().toString());
                        admNum = Integer.parseInt(str);
                    } else {
                        admNum = 1;
                    }
                }
                if (admNum > 9) {
                    Utils.swipeUp("301 1145 320 249");
                    Utils.sleep(3000L);
                    isfrist++;
                    surplusNum = admNum - 9;
                    tv_nameList.clear();
                    findMemberName(messageEntities, taskEntry, membersInfoList, groupName, groupId, surplusNum, true);
                } else {
                    index = admNum;
                }
                isfrist = 1;
            } else {
                if (isadmin) {
                    index = surplusNum;
                }
            }

            degree = index;
            if (num <= memNum + admNum) {
                handler.sendEmptyMessage(1);
            } else {
                // 取出列表 与数据库数据对比
                LogUtils.logInfo("取出列表 与数据库数据对比");
                if (degree < tv_nameList.size() - 1) {
                    if (tv_nameList.get(degree).getText().equals("QQ小冰")) {
                        degree = degree + 1;
                    }
                    LogUtils.logInfo(degree + "   " + tv_nameList.get(degree).getText());
                    methisfind(messageEntities, taskEntry, groupName, groupId, degree, tv_nameList);
                } else {
                    Utils.swipeUp("304 1027 333 568");
                    Utils.sleep(2000L);
                    tv_nameList.clear();
                    findMemberName(messageEntities, taskEntry, membersInfoList, groupName, groupId, 0, true);
                }
            }
        }
    }

    private void methisfind(List<MessageEntity> messageEntities, TaskEntry taskEntry, String groupName, String groupId, int index, List<AccessibilityNodeInfo> tv_nameList) {
        String memberName = tv_nameList.get(index).getText() + "";
//        LogUtils.logInfo("  " + operator.isExists(database, DBConfig.TB_GROUP_MEMBERS, DBConfig.C_GROUP_ID, DBConfig.C_MEMBERS_NAME,
//                DBConfig.C_GROUP_ID + " =? and " + DBConfig.C_MEMBERS_NAME + " =? ", groupId, Utils.getBASE64(memberName))
//                + "  Utils.getBASE64(memberName) = " + Utils.getBASE64(memberName));
        List<MembersInfo> membersInfos = DataSupport.where("wxsign = ? and groupId = ? and membersName = ?",
                taskEntry.getWx_sign(), groupId, Utils.getBASE64(memberName)).find(MembersInfo.class);
        String name = memberName.subSequence(0, 1) + "";
        LogUtils.logInfo("第一个字符 name = " + name);
        boolean istrue = false;
        if (name.equals("a") || name.equals("A")) {
            istrue = true;
        }
        if (!(membersInfos.size() > 0) && !istrue) {
            getDetailsAndSend(messageEntities, taskEntry, groupName, groupId, index, tv_nameList, memberName);
        } else {
            index++;
            tv_nameList.clear();
            findMemberName(messageEntities, taskEntry, null, groupName, groupId, index, false);
        }
    }

    private void getDetailsAndSend(List<MessageEntity> messageEntities, TaskEntry taskEntry, String groupName, String groupId, int index, List<AccessibilityNodeInfo> tv_nameList, String memberName) {
        // 不存在 点击进入详情页面
        Utils.clickCompone(tv_nameList.get(index));
        Utils.sleep(3000L);

        // 获取该成员的QQ id
        List<AccessibilityNodeInfo> textList = Utils.findViewListByType(service, TextView.class.getName());
        String memberid = "";
        if (textList.size() > 0) {
            int ui = 0;
            boolean isfind = true;
            while (isfind) {
                if (!TextUtils.isEmpty(textList.get(ui).getText())) {
                    String qzone = textList.get(ui).getText() + "";
                    if (qzone.contains("群聊等级")) {
                        memberid = textList.get(ui - 3).getText().toString();
                        LogUtils.logInfo("memberid=" + memberid);
                        isfind = false;
                    }
                }
                ui++;
                Utils.sleep(2000L);
                if (ui == 10) {
                    isfind = false;
                    memberid = "-";
                }
            }
        }

        AccessibilityNodeInfo sendBtn = Utils.findViewByText(service, Button.class.getName(), "发消息");
        if (sendBtn != null) {
            LogUtils.logInfo("点击发消息");
            Utils.clickCompone(sendBtn);
            Utils.sleep(3000L);
            isNum = index;
            // android.widget.EditText
            AccessibilityNodeInfo edit = Utils.findViewByType(service, EditText.class.getName());
            if (edit != null) {
                LogUtils.logInfo("找到 输入框 ");
                SendMsgType(messageEntities, taskEntry, Utils.getBASE64(memberName), memberid, groupName, groupId, 0);
            }
        } else {
            Utils.pressBack(service);
            Utils.sleep(3000L);
            findMemberName(messageEntities, taskEntry, null, groupName, groupId, index + 1, false);
        }
    }

    private void SendMsgType(List<MessageEntity> messageEntities, TaskEntry taskEntry, String membernick, String membersId, String groupName, String groupId, int msgLength) {
        if (messageEntities.size() > 0) {
            if (msgLength < messageEntities.size()) {
                MessageEntity messageEntity = messageEntities.get(msgLength);
                switch (messageEntity.getType()) {
                    case SEND_PHOTO:
                        LogUtils.logInfo("發送圖片  url=" + messageEntity.getImgUrl());
                        sendAndDownloadImg(taskEntry, taskEntry.getWx_sign(), messageEntities, messageEntity, groupName, groupId, membernick, membersId);
                        break;

                    case SEND_TEXT:
                        LogUtils.logInfo("發送文本");
                        List<AccessibilityNodeInfo> list = Utils.findViewListByType(service, RelativeLayout.class.getName());
                        LogUtils.logInfo("group   %%%%%%%%------%%%%%%%" + list.get(list.size() - 2).getChild(list.get(list.size() - 2).getChildCount() - 1).toString());

                        Utils.sleep(2000);
                        sendTextMsg(taskEntry, taskEntry.getWx_sign(), messageEntity.getSc_id(), 1, groupName, groupId, membernick, messageEntity.getText(), membersId);
                        HttpHandler.qunfaFinish(taskEntry.getWx_sign(), messageEntity.getSc_id(), membersId);
                        degree++;
                        sendTypeMsg(taskEntry);
                        break;
                }
            }
        }
    }

    int size = 0;

    private void sendAndDownloadImg(TaskEntry taskEntry, String wx_sign, List<MessageEntity> messageEntityList,
                                    MessageEntity messageEntity, String groupName, String groupId, String memberName, String membersId) {
        if (!TextUtils.isEmpty(messageEntity.getImgUrl())) {
            List<String> datas = new ArrayList<>();
            String[] imgUrls = messageEntity.getImgUrl().split("@@@");
            size = imgUrls.length;
            LogUtils.logInfo("   imgUrls.length= " + imgUrls.length);
            List<String> urlList = Arrays.asList(imgUrls);
            Utils.sleep(2000);
            downloadImage(taskEntry, wx_sign, messageEntityList, messageEntity, datas, urlList, groupName, groupId, memberName, membersId, 0);
        }
    }

    private void downloadImage(final TaskEntry taskEntry, final String wx_sign, final List<MessageEntity> messageEntityList,
                               final MessageEntity messageEntity, final List<String> datas, final List<String> urlList,
                               final String groupName, final String groupId, final String memberName, final String membersId, final int exponent) {
        if (exponent < urlList.size()) {
            String url = urlList.get(exponent);
//            Utils.delImageToPhoto();
            LogUtils.logInfo("  下载图片 " + url);
            HttpTask.getInstance().download(url, new HttpTask.HttpCallback() {
                @Override
                public void onSuccess(String data) {
                    if (!TextUtils.isEmpty(data)) {
                        LogUtils.logInfo("  yu   data     =" + data);
                        File file = new File(data);
                        Utils.saveImageToPhoto(service, file);
                        datas.add(data);
                    }
                }

                @Override
                public void onFailure(String errMsg) {
                    LogUtils.logInfo("  err = " + errMsg);
                }

                @Override
                public void onFinished() {
                    LogUtils.logInfo("重复执行图片下载");
                    downloadImage(taskEntry, wx_sign, messageEntityList, messageEntity, datas, urlList, groupName, groupId, memberName, membersId, (exponent + 1));
                }
            });
        } else {
            sendPhoto(taskEntry, messageEntityList, wx_sign, messageEntity.getSc_id(), groupName, groupId, datas, memberName, membersId);
            Utils.sleep(2000);
        }
    }

    private void sendPhoto(TaskEntry taskEntry, List<MessageEntity> messageEntityList, String wx_sign, String sc_id,
                           String groupName, String groupId, List<String> datas, String memberName, String membersId) {
        LogUtils.logInfo("     发送图片     datas= " + datas.size());
        if (datas.size() > 0) {
            String phonetype = Utils.getSystemModel();
            AccessibilityNodeInfo groupChat = Utils.findViewByDesc(service, "聊天设置");
            if (groupChat != null) {
                boolean isfind = true;
                int yi = 0;
                while (isfind) {
                    AccessibilityNodeInfo photos = Utils.findViewByText(service, Button.class.getName(), "相册");
                    LogUtils.logInfo("点击相册按钮");
                    if (photos != null) {
                        Utils.clickCompone(photos);
                        Utils.sleep(3000);
                        isfind = false;
                    } else {
                        switch (phonetype) {
                            // 點擊發送圖片圖標
                            case Constants.HONOR_PHONE_MODEL:
                                Utils.tapScreenXY("154 1185");
                                Utils.sleep(3000);
                                break;
                            case Constants.RAMOS_PHONE_MODEL:
                                Utils.tapScreenXY("225 1750");
                                Utils.sleep(3000);
                                break;
                        }
                    }
                    yi++;
                    if (yi == 4) {
                        isfind = false;
//                        getSendMsg(taskEntry);
                    }
                }
            }
            AccessibilityNodeInfo camera = Utils.findViewByText(service, "Camera");
            AccessibilityNodeInfo photo = Utils.findViewByText(service, "QQPhoto");
            if (camera != null) {
                Utils.clickComponeByXY(camera);
                Utils.sleep(4000);
                sendPhotoMsg(taskEntry, wx_sign, sc_id, 0, groupName, groupId, datas, memberName, membersId);
            } else if (photo != null) {
                Utils.clickComponeByXY(photo);
                Utils.sleep(4000);
                sendPhotoMsg(taskEntry, wx_sign, sc_id, 0, groupName, groupId, datas, memberName, membersId);
            } else {
                Utils.clickCompone(Utils.findViewByText(service, "取消"));//com.tencent.mobileqq:id/ivTitleBtnRightText
                Utils.sleep(2000);

                Utils.pressBack(service);
                Utils.sleep(2000);
            }
        }
    }

    private void sendPhotoMsg(TaskEntry taskEntry, String sign, String sc_id, int type, String grouopName,
                              String groupId, List<String> datas, String memberName, String membersId) {
        AccessibilityNodeInfo imges = Utils.findViewById(service, "com.tencent.mobileqq:id/photo_list_gv");
        if (imges != null) {
            LogUtils.logInfo("size = " + imges.getChildCount() + "    ,   ");
            List<AccessibilityNodeInfo> checkes = Utils.findViewListByType(service, "android.widget.CheckBox");
            if (checkes != null && checkes.size() > 0) {
                LogUtils.logInfo("CheckBox size = " + checkes.size() + "    ,   ");
                if (checkes.size() == 1) {
                    Utils.clickCompone(checkes.get(0));
                    Utils.sleep(2000);
                } else {
                    int length = 0;
                    if (size > 0) {
                        length = size;
                    } else {
                        length = checkes.size() - 2;
                    }
                    for (int i = 0; i < length; i++) {
                        Utils.clickCompone(checkes.get(i));
                        Utils.sleep(2000);
                    }
                }
                Utils.clickCompone(Utils.findViewByText(service, "发送"));
                Utils.sleep(3000);
            }
            AccessibilityNodeInfo failImg = Utils.findViewById(service, "com.tencent.mobileqq:id/chat_item_fail_icon");
            if (failImg != null) {
                FileUtils.deleteFiles(datas);
                Utils.delImageToPhoto();
                LogUtils.logInfo("返回到消息页");
                Utils.pressBack(service);
                Utils.sleep(3000);

                if (Utils.findViewByDesc(service, "聊天设置") != null) {
                    Utils.clickCompone(Utils.findViewByDesc(service, "返回消息"));
                    Utils.sleep(3000L);
                }
                membersNum++;
                sendTypeMsg(taskEntry);
            } else {
                if (Utils.findViewByTextMatch(service, "无法上传") != null) {
                    Utils.findViewByText(service, "我知道了");
                    Utils.sleep(2000L);

                    Utils.clickCompone(Utils.findViewByText(service, "取消"));//com.tencent.mobileqq:id/ivTitleBtnRightText
                    Utils.sleep(2000);

                    Utils.pressBack(service);
                    Utils.sleep(2000);
                } else {
                    FileUtils.deleteFiles(datas);
                    Utils.delImageToPhoto();
                    LogUtils.logInfo("返回到消息页");
                    Utils.pressBack(service);
                    Utils.sleep(3000);

                    if (Utils.findViewByDesc(service, "聊天设置") != null) {
                        Utils.clickCompone(Utils.findViewByDesc(service, "返回消息"));
                        Utils.sleep(3000L);
                    }
                    addDataToDB(type, taskEntry.getWx_sign(), grouopName, groupId, memberName, membersId);
                    HttpHandler.qunfaFinish(sign, sc_id, membersId);
                    sendNum++;
                    sendTypeMsg(taskEntry);
                }
            }
        }
    }

    private void addDataToDB(int type, String wxsign, String groupName, String groupId, String memberName, String membersId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String date = formatter.format(curDate);
        LogUtils.logInfo("存入数据库  groupName=" + groupName + " groupId = " + groupId + "  membersId = " + membersId + "  memberName= " + memberName + "  date= " + date);
        MembersInfo membersInfo = new MembersInfo();
        membersInfo.setGroupId(groupId);
        membersInfo.setMembersName(memberName);
        membersInfo.setMemberId(membersId);
        membersInfo.setMemberSendTime(date);
        membersInfo.setGroupName(groupName);
        membersInfo.setWxsign(wxsign);
        List<MembersInfo> membersInfos = DataSupport.where("wxsign = ? and groupId= ? and memberId = ? ", wxsign, groupId, membersId).find(MembersInfo.class);
        if (membersInfos.size() == 0) {
            membersInfo.saveThrows();
        } else {
            ContentValues values = new ContentValues();
            values.put("memberSendTime", date);
            values.put("membersName", memberName);
            values.put("groupName", groupName);
            values.put("wxsign", wxsign);
            DataSupport.updateAll(MembersInfo.class, values, "wxsign = ? and groupId= ? and memberId = ? ", wxsign, groupId, membersId);
        }
    }

    /***
     *  输入文本并发送
     * @param sendmsg
     */
    private void sendTextMsg(TaskEntry taskEntry, String sign, String sc_id, int type, String groupName, String groupId, String membernick, String sendmsg, String membersId) {
        if (!TextUtils.isEmpty(sendmsg)) {
            AccessibilityNodeInfo editText = Utils.findViewByType(service, EditText.class.getName());
            if (editText != null) {
                Utils.componeFocus(editText);
                Utils.sleep(2000L);
                Utils.selectAllText(editText);
                Utils.sleep(2000L);
                Utils.inputText(service, editText, sendmsg);
                Utils.sleep(2000L);
                AccessibilityNodeInfo sendBtn = Utils.findViewByText(service, Button.class.getName(), "发送");
                if (sendBtn != null && sendBtn.isEnabled()) {
                    Utils.clickCompone(sendBtn);
                    Utils.sleep(3000L);
                }
                addDataToDB(0, taskEntry.getWx_sign(), groupName, groupId, membernick, membersId);
                Utils.pressBack(service);
                Utils.sleep(2000L);
            } else {
                Utils.pressBack(service);
                Utils.sleep(2000);
            }
        } else {
            Utils.pressBack(service);
            Utils.sleep(2000);

        }
    }
}
