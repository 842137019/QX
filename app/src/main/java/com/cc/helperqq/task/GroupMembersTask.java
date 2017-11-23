package com.cc.helperqq.task;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import com.cc.helperqq.entity.GroupMembersInfo;
import com.cc.helperqq.entity.MembersInfo;
import com.cc.helperqq.entity.MessageEntity;
import com.cc.helperqq.entity.ReplyMsgInfo;
import com.cc.helperqq.entity.TaskEntry;
import com.cc.helperqq.MyApplication;
import com.cc.helperqq.http.HttpHandler;
import com.cc.helperqq.http.HttpTask;
import com.cc.helperqq.service.HelperQQService;
import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.FileUtils;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * Created by fangying on 2017/11/8.
 */

public class GroupMembersTask {
    private HelperQQService service;
    private Handler handler;
    private static final int SEND_PHOTO = 1;
    private static final int SEND_TEXT = 2;
    private int degree = 0;// 发送消息次数
    private int isNum = 0;
    private int groupNum = 0; // 取第几的个群
    private int memberIndex = 0;// 取第几的个成员

    public GroupMembersTask(HelperQQService service, Handler handler) {
        this.service = service;
        this.handler = handler;
    }

    /***
     * 获取回复数据
     * @param taskEntry
     */
    public void sendTypeMsg(final TaskEntry taskEntry) {
        if( !Utils.isTragetActivity(Constants.QQ_HOME_ACTIVITY)){
            Utils.startPage(Constants.QQ_HOME_ACTIVITY);
            Utils.sleep(5000L);
        }

        String groupmembers = "/sdcard" + File.separator + Constants.CACHE_PATH_NAME + File.separator + "GroupMembers" + File.separator + taskEntry.getWx_sign() + ".txt";
        File file = new File(groupmembers);
        if (file.exists()) {
            FileUtils.deleteFile(groupmembers);
        }

        LogUtils.logInfo(" degree = " + degree);
        if (degree < 5) {
            if (taskEntry != null) {
                LogUtils.logInfo("      请求获取回复数据       ");
                HttpHandler.getqunfaMsg(taskEntry.getWx_sign(), "", new HttpTask.HttpCallback() {
                    @Override
                    public void onSuccess(String data) {
                        LogUtils.logInfo("data  =" + data);
                        if (!TextUtils.isEmpty(data) && data.length() > 4) {
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
        } else {
            AccessibilityNodeInfo canleBtn = Utils.findViewByText(service, Button.class.getName(), "取消");
            if (canleBtn != null) {
                Utils.clickComponeByXY(canleBtn);
                Utils.sleep(2000L);
            }
            LogUtils.logInfo(" 超过5条消息  执行完成 ");
            degree = 0;
            groupNum =0;
            handler.sendEmptyMessage(1);
        }
    }

    public void sendGroupMember(List<MessageEntity> messageEntities, TaskEntry taskEntry) {
        Utils.launcherApp(service, Constants.APP_NAME);
        Utils.sleep(3000L);

//        AccessibilityNodeInfo contacts = Utils.findViewByTextMatch(service, "联系人");
//        if (contacts == null) {
//            handler.sendEmptyMessage(1);
//            return;
//        }
//        Utils.clickCompone(contacts);
//        Utils.sleep(2000);
//
//        Utils.clickCompone(Utils.findViewByTextMatch(service, "群"));
//        Utils.sleep(5000);

        AccessibilityNodeInfo editSou = Utils.findViewByType(service, EditText.class.getName());
        if (editSou != null) {
            Utils.clickComponeByXY(editSou);
            Utils.sleep(2000);
            // 数据库数据不对   抓取群数据对比
            String wxsign = taskEntry.getWx_sign();
            List<GroupInfo> groupInfos = DataSupport.where("wxsign = ? and groupType = ?", wxsign, "success").find(GroupInfo.class);
            LogUtils.logInfo("当前wxsign 的加群个数 = " + groupInfos.size() + " groupNum = " + groupNum);

            if (degree < 5 && groupNum < groupInfos.size()) {
                if (groupInfos.size() > 0) {
//                    if (Utils.startMsgView(service, groupId, "1")) { // 是否打开群
                    // 185179045   125817813
                    inputGroupId(messageEntities, taskEntry, groupInfos);
                } else {
                    // 数据库没有数据执行 在群列表点击群昵称进入
                    LogUtils.logInfo("数据库没有数据执行  在群列表点击群昵称进入");
                    clickGroupName(messageEntities, taskEntry, 0);
                }
            } else {
                AccessibilityNodeInfo canleBtn = Utils.findViewByText(service, Button.class.getName(), "取消");
                if (canleBtn != null) {
                    Utils.clickComponeByXY(canleBtn);
                    Utils.sleep(2000L);
                }
                degree = 0;
                groupNum =0;
                LogUtils.logInfo("执行完成");
                handler.sendEmptyMessage(1);
            }
        } else {
            degree = 0;
            groupNum =0;
            LogUtils.logInfo("执行完成");
            handler.sendEmptyMessage(1);
        }
    }

    /***
     * 输入群id
     * @param messageEntities
     * @param taskEntry
     * @param groupInfos
     */
    private void inputGroupId(List<MessageEntity> messageEntities, TaskEntry taskEntry, List<GroupInfo> groupInfos) {
        LogUtils.logInfo("输入群id");
        GroupInfo groupInfo = groupInfos.get(groupNum);
//                if (groupInfo.getGroupType().equals("success")) {
        String groupId = groupInfo.getGroupId();
        List<MembersInfo> membersInfos = DataSupport.where("wxsign = ? and groupId= ?", taskEntry.getWx_sign(), groupId).find(MembersInfo.class);
        List<GroupMembersInfo> groupMembersInfos = DataSupport.where("wxsign = ? and groupId = ?", taskEntry.getWx_sign(), groupId).find(GroupMembersInfo.class);
        LogUtils.logInfo("当前第" + groupNum + "个群  " + "GroupId=" + groupId);
        if (groupNum < groupInfos.size()) {
            AccessibilityNodeInfo search_keyword = Utils.findViewById(service, "com.tencent.mobileqq:id/et_search_keyword");
            if (search_keyword != null) {
                Utils.inputText(groupInfo.getGroupId());
                Utils.sleep(6000);
                // com.tencent.mobileqq:id/title
                AccessibilityNodeInfo title = Utils.findViewByTextMatch(service, "来自:群");
                if (title != null) {
                    Utils.clickComponeByXY(title);
                    Utils.sleep(3000L);
                    LogUtils.logInfo("从数据库进入  " + groupId);
                    OpenMemberList(messageEntities, taskEntry, groupId);
                } else {
                    // 未打开群
                    LogUtils.logInfo("该群不存在或尚未添加成功" + groupId);
                    groupNum++;
                    Utils.sleep(2000L);
                    AccessibilityNodeInfo clearBtn = Utils.findViewById(service, "com.tencent.mobileqq:id/ib_clear_text");
                    if (clearBtn != null) {
                        Utils.clickComponeByXY(clearBtn);
                        Utils.sleep(2000);
                    }
                    inputGroupId(messageEntities, taskEntry, groupInfos);
                }
            }
        } else {
            groupNum++;
            inputGroupId(messageEntities, taskEntry, groupInfos);
        }

    }

    private void clickGroupName(List<MessageEntity> messageEntities, TaskEntry taskEntry,
                                int leng) {
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
            Utils.sleep(2000);
            AccessibilityNodeInfo groupChat = Utils.findViewByDesc(service, "群资料卡");
            if (groupChat != null) {
                Utils.clickCompone(groupChat);
                Utils.sleep(4000L);
                List<AccessibilityNodeInfo> list = Utils.findViewListByType(service, TextView.class.getName());
                String groupId = list.get(0).getText().toString();

                OpenMemberList(messageEntities, taskEntry, groupId);
            }
        }
    }

    private void OpenMemberList(List<MessageEntity> messageEntities, TaskEntry
            taskEntry, String groupId) {
        closeTheGroupAndGroupClosureAnnouncement(messageEntities, taskEntry);
        String groupName = findGroupDataControlAndGroupNickname(messageEntities, taskEntry, groupId);
        AccessibilityNodeInfo memberNumstr = Utils.findViewByText(service, "名成员");
        int memberNum = 0;
        if (memberNumstr != null) {
            String numstr = memberNumstr.getText() + "";
            LogUtils.logInfo("获取成员数量 numstr= " + numstr);
            memberNum = Integer.parseInt(Utils.getNumbers(numstr));
            List<MembersInfo> membersInfos = DataSupport.where("wxsign = ? and groupId= ?", taskEntry.getWx_sign(), groupId).find(MembersInfo.class);
            if (membersInfos.size() != 0) {
                int memNum = membersInfos.size();
                LogUtils.logInfo("当前群组已发送条数 memNum = " + memNum);
                if (memNum >= memberNum) {
                    groupNum++;
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
                    clickIntoGetGroupMembers(messageEntities, taskEntry, groupId, groupName, memberNumstr, memberNum);
                }
            } else {
                clickIntoGetGroupMembers(messageEntities, taskEntry, groupId, groupName, memberNumstr, memberNum);
            }
        }
    }

    /***
     * 关闭群公告和群封停
     * @param messageEntities
     * @param taskEntry
     */
    private void closeTheGroupAndGroupClosureAnnouncement
    (List<MessageEntity> messageEntities, TaskEntry taskEntry) {
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
            groupNum++;
            sendGroupMember(messageEntities, taskEntry);
        }
    }

    /**
     * 查找群资料控件和群昵称
     *
     * @param messageEntities
     * @param taskEntry
     * @param groupId
     */
    private String findGroupDataControlAndGroupNickname
    (List<MessageEntity> messageEntities, TaskEntry taskEntry, String groupId) {
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
                groupNum++;
                sendGroupMember(messageEntities, taskEntry);
            }
        }

        LogUtils.logInfo("获取群昵称");
        Utils.sleep(2000L);
        String groupName = "";
        if (!TextUtils.isEmpty(groupId)) {
            List<AccessibilityNodeInfo> list = Utils.findViewListByType(service, TextView.class.getName());
            if (list.size() > 1) {
                int index = 0;
                isfind = true;
                while (isfind) {
                    if (!TextUtils.isEmpty(list.get(index).getText())) {
                        String id = list.get(index).getText() + "";
                        if (id.equals(groupId)) {
                            groupName = list.get(0).getText().toString();
                            LogUtils.logInfo(" groupId = " + id + "  groupName = " + groupName);
                            isfind = false;
                        }
                        index++;
                        Utils.sleep(2000L);
                        if (index == list.size()) {
                            groupName = "-";
                            isfind = false;
                        }
                    }
                }
            }
        }
        return groupName;
    }

    /***
     *  点击进入获取群成员
     * @param messageEntities
     * @param taskEntry
     * @param groupId
     * @param groupName
     * @param memberNum
     */
    private void clickIntoGetGroupMembers(List<MessageEntity> messageEntities, TaskEntry
            taskEntry,
                                          String groupId, String groupName, AccessibilityNodeInfo memberNumInfo, int memberNum) {
        Utils.clickCompone(memberNumInfo);
        Utils.sleep(3000);
        List<GroupMembersInfo> groupMembersInfoList = DataSupport.where("wxsign = ? and groupId = ? ",
                taskEntry.getWx_sign(), groupId).find(GroupMembersInfo.class);
        LogUtils.logInfo("数据库中保存的群成员数目 = " + groupMembersInfoList.size());
        if (memberNum > 100) {
            if (((memberNum - 50) < groupMembersInfoList.size()) && (groupMembersInfoList.size() < (memberNum + 50))) {
                // 不去读取文件
            } else {
                // 读取文件
                getReadFileGroupMembers(taskEntry, groupName);
            }
        } else {
            getReadFileGroupMembers(taskEntry, groupName);
        }
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

        List<MembersInfo> infos = DataSupport.where("wxsign = ?", taskEntry.getWx_sign()).find(MembersInfo.class);
        if (infos.size() > 0) {
            List<MembersInfo> membersInfoList = new ArrayList<>();
            for (MembersInfo membersInfo : infos) {
                if (membersInfo.getGroupId().equals(groupId)) {
                    membersInfoList.add(membersInfo);
                }
            }
            LogUtils.logInfo("membersInfoList  size =" + membersInfoList.size());
//            isfindTvName(messageEntities, taskEntry);
//            List<AccessibilityNodeInfo> tv_nameList = Utils.findViewListById(service, "com.tencent.mobileqq:id/tv_name");
//            if (tv_nameList != null && tv_nameList.size() > 0) {
            findMemberName(messageEntities, taskEntry, membersInfoList, groupName, groupId);
//            } else {
//                Utils.pressBack(service);
//                Utils.sleep(3000L);
//                Utils.pressBack(service);
//                Utils.sleep(3000L);
//                Utils.pressBack(service);
//                Utils.sleep(3000L);
//                groupNum++;
//                sendGroupMember(messageEntities, taskEntry);
//            }
        } else {
            LogUtils.logInfo("数据为空");
//            isfindTvName(messageEntities, taskEntry);
            findMemberName(messageEntities, taskEntry, null, groupName, groupId);
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
                groupNum++;
                sendGroupMember(messageEntities, taskEntry);
                isfind = false;
            }
        }
    }

    /***
     * 查找成员
     * @param messageEntities
     * @param taskEntry
     * @param membersInfoList
     * @param groupName
     * @param groupid
     */
    private void findMemberName(List<MessageEntity> messageEntities, TaskEntry taskEntry,
                                List<MembersInfo> membersInfoList, String groupName, String groupid) {
        LogUtils.logInfo("查找成员");
        List<GroupMembersInfo> groupMembersInfos = DataSupport.where("wxsign = ? and groupId = ?", taskEntry.getWx_sign(), groupid).find(GroupMembersInfo.class);
        String memberId = groupMembersInfos.get(memberIndex).getMemberId();
        List<MembersInfo> membersInfos = DataSupport.where("wxsign = ? and groupId = ? and memberId = ?",
                taskEntry.getWx_sign(), groupid, memberId).find(MembersInfo.class);
        AccessibilityNodeInfo editInfo = Utils.findViewByType(service, EditText.class.getName());
        if (editInfo != null && groupMembersInfos.size() > 0) {
            LogUtils.logInfo("群成员数据 groupMembersInfos =" + groupMembersInfos.size());
            if (memberIndex < groupMembersInfos.size() && membersInfos.size() == 0) {
                Utils.clickComponeByXY(editInfo);
                Utils.sleep(3000L);
//                String memberId = DetermineWhetherItHasBeenSent(taskEntry, groupid, groupMembersInfos);
                LogUtils.logInfo("未发送过  输入id");
                AccessibilityNodeInfo ed = Utils.findViewById(service, "com.tencent.mobileqq:id/et_search_keyword");
                if (ed != null) {
                    if (!TextUtils.isEmpty(ed.getText())) {
                        AccessibilityNodeInfo clear = Utils.findViewById(service, "com.tencent.mobileqq:id/ib_clear_text");
                        if (clear != null) {
                            Utils.clickComponeByXY(clear);
                            Utils.sleep(2000L);
                        }
                    }
                    Utils.inputText(memberId);
                    Utils.sleep(3000L);
                    AccessibilityNodeInfo tvName = Utils.findViewById(service, "com.tencent.mobileqq:id/tv_name");
                    if (tvName != null) {
                        String memberName = tvName.getText() + "";
                        LogUtils.logInfo("memberName = " + memberName + " memberId = " + memberId);
                        theTypeofJudgmentAndWhetherItHasBeenSent(messageEntities, taskEntry, groupName, groupid, memberName, memberId);
                    } else {
                        memberIndex++;
                        Utils.clickCompone(Utils.findViewByText(service, Button.class.getName(), "取消"));
                        Utils.sleep(2000);
                        findMemberName(messageEntities, taskEntry, membersInfoList, groupName, groupid);
                    }
                }
            } else {
                memberIndex++;
                GroupMembersInfo groupMembersInfo = groupMembersInfos.get(0);
                int id = groupMembersInfo.getId();
                ContentValues values = new ContentValues();
                values.put("sendOrNot", "是");
                DataSupport.update(GroupMembersInfo.class, values, id);
                Utils.clickCompone(Utils.findViewByText(service, Button.class.getName(), "取消"));
                Utils.sleep(2000);
                findMemberName(messageEntities, taskEntry, membersInfoList, groupName, groupid);
//
////                    LogUtils.logInfo("已发送过 切换下一个成员QQ");
////                    memberIndex++;
////                    AccessibilityNodeInfo clear = Utils.findViewById(service, "com.tencent.mobileqq:id/ib_clear_text");
////                    if (clear != null) {
////                        Utils.clickComponeByXY(clear);
////                        Utils.sleep(2000L);
////                        DetermineWhetherItHasBeenSent(taskEntry, groupid, groupMembersInfos);
////                    }
//                }
            }
        } else {
            groupNum++;
            Utils.clickCompone(Utils.findViewByText(service, Button.class.getName(), "取消"));
            Utils.sleep(2000);
            Utils.pressBack(service);
            Utils.sleep(3000L);
            Utils.pressBack(service);
            Utils.sleep(3000L);
            Utils.pressBack(service);
            Utils.sleep(3000L);
            sendGroupMember(messageEntities, taskEntry);
        }
    }

//    /***
//     * 判断是否已发送过
//     * @param taskEntry
//     * @param grouId
//     * @param groupMembersInfos
//     */
//    private String DetermineWhetherItHasBeenSent(TaskEntry taskEntry, String grouId, List<GroupMembersInfo> groupMembersInfos) {
//        LogUtils.logInfo("判断是否已发送过");
//        String memberId = groupMembersInfos.get(memberIndex).getMemberId();
//        List<MembersInfo> membersInfos = DataSupport.where("wxsign = ? and groupId = ? and memberId = ?",
//                taskEntry.getWx_sign(), grouId, memberId).find(MembersInfo.class);
//        if (membersInfos.size() == 0) {
//            LogUtils.logInfo("未发送过  输入id");
//            AccessibilityNodeInfo ed = Utils.findViewById(service, "com.tencent.mobileqq:id/et_search_keyword");
//            if (ed != null) {
//                Utils.inputText(memberId);
//                Utils.sleep(3000L);
//            }
//        } else {
//            LogUtils.logInfo("已发送过 切换下一个成员QQ");
//            memberIndex++;
//            AccessibilityNodeInfo clear = Utils.findViewById(service, "com.tencent.mobileqq:id/ib_clear_text");
//            if (clear != null) {
//                Utils.clickComponeByXY(clear);
//                Utils.sleep(2000L);
//                DetermineWhetherItHasBeenSent(taskEntry, grouId, groupMembersInfos);
//            }
//        }
//        return memberId;
//    }

    /***
     * 判断类型及是否已被发送过
     * @param messageEntities
     * @param taskEntry
     * @param groupName
     * @param groupId
     * @param memberName
     * @param memberId
     */
    private void theTypeofJudgmentAndWhetherItHasBeenSent
    (List<MessageEntity> messageEntities, TaskEntry taskEntry,
     String groupName, String groupId, String memberName, String memberId) {
        List<MembersInfo> membersInfos = DataSupport.where("wxsign = ? and groupId = ? and memberId = ?",
                taskEntry.getWx_sign(), groupId, memberId).find(MembersInfo.class);
        String name = memberName.subSequence(0, 1) + "";
        LogUtils.logInfo("第一个字符 name = " + name);
        boolean istrue = false;
        if (name.equals("a") || name.equals("A")) {
            MembersInfo membersInfo = new MembersInfo();
            membersInfo.setGroupId(groupId);
            membersInfo.setMembersName(memberName);
            membersInfo.setMemberId(memberId);
            membersInfo.setMemberSendTime("");
            membersInfo.setGroupName(groupName);
            membersInfo.setWxsign(taskEntry.getWx_sign());
            membersInfo.saveThrows();
            istrue = true;
        }
        if (!(membersInfos.size() > 0) && !istrue) {
            getDetailsAndSend(messageEntities, taskEntry, groupName, groupId, 0, memberName, memberId);
        } else {
            memberIndex++;
            findMemberName(messageEntities, taskEntry, null, groupName, groupId);
        }
    }

    /***
     * 获取详细信息并发送
     * @param messageEntities
     * @param taskEntry
     * @param groupName
     * @param groupId
     * @param index
     * @param memberName
     * @param memberid
     */
    private void getDetailsAndSend(List<MessageEntity> messageEntities, TaskEntry
            taskEntry, String groupName,
                                   String groupId, int index, String memberName, String memberid) {
        // 不存在 点击进入详情页面
        Utils.clickCompone(Utils.findViewByText(service, memberName));
        Utils.sleep(3000L);

        // 获取该成员的QQ id
//        List<AccessibilityNodeInfo> textList = Utils.findViewListByType(service, TextView.class.getName());
//        String memberid = "";
//        if (textList.size() > 0) {
//            int ui = 0;
//            boolean isfind = true;
//            while (isfind) {
//                if (!TextUtils.isEmpty(textList.get(ui).getText())) {
//                    String qzone = textList.get(ui).getText() + "";
//                    if (qzone.contains("群聊等级")) {
//                        memberid = textList.get(ui - 3).getText().toString();
//                        LogUtils.logInfo("memberid=" + memberid);
//                        isfind = false;
//                    } else if (ui != 0 && Utils.isNumber(qzone)) {
//                        memberid = qzone;
//                        LogUtils.logInfo("memberid=" + memberid);
//                        isfind = false;
//                    }
//                }
//                ui++;
//                Utils.sleep(2000L);
//                if (ui == 10) {
//                    isfind = false;
//                    memberid = "-";
//                }
//            }
//        }

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
            memberIndex++;
            findMemberName(messageEntities, taskEntry, null, groupName, groupId);
        }
    }

    /***
     *  获取并保存群成员数据
     * @param taskEntry
     * @param groupName
     */
    private void getReadFileGroupMembers(TaskEntry taskEntry, String groupName) {
        LogUtils.logInfo("获取群成员数据");
//        Utils.sleep(15 * 1000L);
//        String groupmembers = "/sdcard" + File.separator + Constants.CACHE_PATH_NAME + File.separator + "GroupMembers" + File.separator + taskEntry.getWx_sign() + ".txt";
//        String string = FileUtils.readStringToFile(groupmembers);
//        if (!TextUtils.isEmpty(string)) {
        String str2 = WhileStringContains(taskEntry.getWx_sign());
        List<GroupMembersInfo> groupMembersInfos = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(str2);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String friendnick = jsonObject.getString("friendnick");
                String memberuin = jsonObject.getString("memberuin");
                String troopuin = jsonObject.getString("troopuin");
                String troopnick = jsonObject.getString("troopnick");
                String sex = jsonObject.getString("sex");

                GroupMembersInfo membersInfo = new GroupMembersInfo();
                membersInfo.setMemberId(memberuin);
                membersInfo.setMembersName(friendnick);
                membersInfo.setGroupId(troopuin);
                membersInfo.setMembersSex(sex);
                membersInfo.setGroupCard(troopnick);
                membersInfo.setSendOrNot("否");
                groupMembersInfos.add(membersInfo);
                membersInfo = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtils.logInfo(" groupMembersInfos size= " + groupMembersInfos.size());
        for (int qi = 0; qi < groupMembersInfos.size(); qi++) {
            List<GroupMembersInfo> membersInfos1 = DataSupport.where("wxsign = ? and groupId = ? and memberId = ?",
                    taskEntry.getWx_sign(), groupMembersInfos.get(qi).getGroupId()
                    , groupMembersInfos.get(qi).getMemberId()).find(GroupMembersInfo.class);
            if (membersInfos1.size() == 0) {
                GroupMembersInfo groupMembersInfo = new GroupMembersInfo();
                groupMembersInfo.setGroupName(groupName);
                groupMembersInfo.setMemberId(groupMembersInfos.get(qi).getMemberId());
                groupMembersInfo.setGroupId(groupMembersInfos.get(qi).getGroupId());
                groupMembersInfo.setWxsign(taskEntry.getWx_sign());
                groupMembersInfo.setMembersName(groupMembersInfos.get(qi).getMembersName());
                groupMembersInfo.setMembersSex(groupMembersInfos.get(qi).getMembersSex());
                groupMembersInfo.setGroupCard(groupMembersInfos.get(qi).getGroupCard());
                groupMembersInfo.setSendOrNot(groupMembersInfos.get(qi).getSendOrNot());
                groupMembersInfo.saveThrows();
            }
        }
//        }
    }

    /***
     * 处理保存本地的群成员数据
     * @param wxsign
     * @return
     */
    @NonNull
    private String WhileStringContains(String wxsign) {
        boolean isok = true;
        String str2 = "";
        while (isok) {
            Utils.sleep(15 * 1000L);
            String groupmembers = "/sdcard" + File.separator + Constants.CACHE_PATH_NAME + File.separator + "GroupMembers" + File.separator + wxsign + ".txt";
            String string = FileUtils.readStringToFile(groupmembers);
            if (!TextUtils.isEmpty(string)) {
                String chars = string.substring(0, string.length() - 1);
                StringBuffer buffer = new StringBuffer(chars);
                buffer.insert(0, "[");
                String str1 = buffer.toString();
                buffer.insert(str1.length(), "]");
                str2 = buffer.toString();
//                LogUtils.logInfo(" str2 = " + str2);
                if (str2.contains("]")) {
                    isok = false;
                }
            }
        }
        return str2;
    }

    /***
     * 选择发送素材类别
     * @param messageEntities
     * @param taskEntry
     * @param membernick
     * @param membersId
     * @param groupName
     * @param groupId
     * @param msgLength
     */
    private void SendMsgType(List<MessageEntity> messageEntities, TaskEntry
            taskEntry, String membernick,
                             String membersId, String groupName, String groupId, int msgLength) {
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

    /***
     * 获取图片下载链接Url
     * @param taskEntry
     * @param wx_sign
     * @param messageEntityList
     * @param messageEntity
     * @param groupName
     * @param groupId
     * @param memberName
     * @param membersId
     */
    private void sendAndDownloadImg(TaskEntry taskEntry, String
            wx_sign, List<MessageEntity> messageEntityList,
                                    MessageEntity messageEntity, String groupName, String groupId, String
                                            memberName, String membersId) {
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

    /***
     * 下载图片
     * @param taskEntry
     * @param wx_sign
     * @param messageEntityList
     * @param messageEntity
     * @param datas
     * @param urlList
     * @param groupName
     * @param groupId
     * @param memberName
     * @param membersId
     * @param exponent
     */
    private void downloadImage(final TaskEntry taskEntry, final String wx_sign,
                               final List<MessageEntity> messageEntityList,
                               final MessageEntity messageEntity, final List<String> datas, final List<String> urlList,
                               final String groupName, final String groupId, final String memberName,
                               final String membersId, final int exponent) {
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

    /***
     * 进入选择发送图片
     * @param taskEntry
     * @param messageEntityList
     * @param wx_sign
     * @param sc_id
     * @param groupName
     * @param groupId
     * @param datas
     * @param memberName
     * @param membersId
     */
    private void sendPhoto(TaskEntry
                                   taskEntry, List<MessageEntity> messageEntityList, String wx_sign, String sc_id,
                           String groupName, String groupId, List<String> datas, String memberName, String
                                   membersId) {
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

    /***
     *  点击发送图片
     * @param taskEntry
     * @param sign 当前帐号唯一标识
     * @param sc_id  发送的素材id
     * @param type 素材类别
     * @param grouopName
     * @param groupId
     * @param datas 图片素材保存本地路径集合
     * @param memberName
     * @param membersId
     */
    private void sendPhotoMsg(TaskEntry taskEntry, String sign, String sc_id, int type, String
            grouopName,
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
                groupNum++;
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
                    LogUtils.logInfo("degree = " + degree);
                    degree++;
                    sendTypeMsg(taskEntry);
                }
            }
        }
    }

    /***
     * 保存已发送成员
     * @param type
     * @param wxsign  当前帐号唯一标识
     * @param groupName 群名称
     * @param groupId 群id
     * @param memberName 成员名称
     * @param membersId 成员id
     */
    private void addDataToDB(int type, String wxsign, String groupName, String groupId, String
            memberName, String membersId) {
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

        List<GroupMembersInfo> groupMembersInfoList = DataSupport.where("wxsign = ? and groupId= ? and memberId = ? ", wxsign, groupId, membersId).find(GroupMembersInfo.class);
        if (groupMembersInfoList.size() > 0) {
            GroupMembersInfo groupMembersInfo = groupMembersInfoList.get(0);
            int id = groupMembersInfo.getId();
            ContentValues values = new ContentValues();
            values.put("sendOrNot", "是");
            DataSupport.update(GroupMembersInfo.class, values, id);
        }

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
    private void sendTextMsg(TaskEntry taskEntry, String sign, String sc_id, int type, String
            groupName,
                             String groupId, String membernick, String sendmsg, String membersId) {
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
