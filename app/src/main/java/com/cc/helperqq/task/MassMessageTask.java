package com.cc.helperqq.task;

import android.content.ContentValues;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cc.helperqq.entity.GroupInfo;
import com.cc.helperqq.entity.MessageEntity;
import com.cc.helperqq.entity.ReplyMsgInfo;
import com.cc.helperqq.entity.TaskEntry;
import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.FileUtils;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.Utils;
import com.cc.helperqq.http.HttpHandler;
import com.cc.helperqq.http.HttpTask;
import com.cc.helperqq.service.HelperQQService;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 发群消息
 * Created by fangying on 2017/9/29.
 */

public class MassMessageTask {
    private HelperQQService service;
    private Handler handler;
    private static final int SEND_PHOTO = 1;
    private static final int SEND_TEXT = 2;
    private int index = 0;
    private int sendNum = 0;

    public MassMessageTask(HelperQQService service, Handler handler) {
        this.handler = handler;
        this.service = service;
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
//                            replyMsg(messageEntities, taskEntry);
                            massGroupMsg(messageEntities, taskEntry);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        HttpHandler.reportError(taskEntry.getWx_sign(), "获取群发數據为空!");
                        handler.sendEmptyMessage(1);
                    }
                }

                @Override
                public void onFailure(String errMsg) {
                    LogUtils.logInfo("err  =" + errMsg);
                    HttpHandler.reportError(taskEntry.getWx_sign(), "获取群发數據失败!");
                    handler.sendEmptyMessage(1);
                }

                @Override
                public void onFinished() {
                }
            });
        }
    }

    public void massGroupMsg(List<MessageEntity> messageEntityList, TaskEntry taskEntry) {
        AccessibilityNodeInfo contacts = Utils.findViewByTextMatch(service, "联系人");
        if (contacts == null) {
            handler.sendEmptyMessage(1);
            return;
        }
        Utils.clickCompone(contacts);
        Utils.sleep(2000);

        Utils.clickCompone(Utils.findViewByTextMatch(service, "群"));
        Utils.sleep(4000);
        List<GroupInfo> groupInfos = DataSupport.where(" wxsign = ? and groupType = ? ", taskEntry.getWx_sign(),"success").find(GroupInfo.class);
        LogUtils.logInfo("当前wxsign 的加群个数 = " + groupInfos.size());
        getStartView(messageEntityList, taskEntry, groupInfos);
    }

    private void getStartView(List<MessageEntity> messageEntityList, TaskEntry taskEntry, List<GroupInfo> groupInfos) {
        if (sendNum < 5 && index < groupInfos.size()) {
            if (groupInfos.size() > 0) {
                GroupInfo groupInfo = groupInfos.get(index);
                if (groupInfo.getGroupType().equals("success")) {
                    String groupId = groupInfo.getGroupId();
                    LogUtils.logInfo("GroupId=" + groupId);
                    if (Utils.startMsgView(service, groupId, "1")) { // 是否打开群
                        LogUtils.logInfo("从数据库进入  " + groupId);
                        getSendMsg(messageEntityList, taskEntry,groupId);
                    } else {
                        // 换下一个群id
                        index++;
                        getStartView(messageEntityList, taskEntry, groupInfos);
                    }
                }
            }else {
                handler.sendEmptyMessage(1);
            }
        }else {
            handler.sendEmptyMessage(1);
        }
    }

    private void getSendMsg(List<MessageEntity> messageEntityList, TaskEntry taskEntry,String groupId) {
//        if (!TextUtils.isEmpty(listText1.get(index).getText().toString().trim())) {
//            String strname = listText1.get(index).getText().toString().trim();
            List<ReplyMsgInfo> replyMsgInfos = DataSupport.where("wxsign= ? and groupId = ?", taskEntry.getWx_sign(), groupId).find(ReplyMsgInfo.class);
            if (replyMsgInfos.size() > 0) {
                ReplyMsgInfo replyMsgInfo = replyMsgInfos.get(0);
                String replyTime = replyMsgInfo.getSendmsgTime();
                LogUtils.logInfo("  replyTime   =  " + replyTime);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                String date = sdf.format(new java.util.Date());
                LogUtils.logInfo("  date   =  " + date);
                if (!replyTime.contains(date)) {
                    getMsgObject(messageEntityList, taskEntry);
                } else {
                    index++;
                    massGroupMsg(messageEntityList, taskEntry);
                }
            } else {
                getMsgObject(messageEntityList, taskEntry);
            }
//        }
    }

    private void getMsgObject(List<MessageEntity> messageEntityList, TaskEntry taskEntry) {
//        Utils.sleep(4000);
//        if (Utils.findViewByTextMatch(service, listText1.get(index).getText().toString()) == null) {
//            Utils.swipeUp("333 960 338 464");
//            Utils.sleep(3000L);
//        }
//
//        LogUtils.logInfo(" listText1 size = " + listText1.size() + "   index = " + index + "    " + listText1.get(index).getText().toString().trim());
//        Utils.clickCompone(listText1.get(index));
//        Utils.sleep(5000);

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
            AccessibilityNodeInfo jinyan = Utils.findViewById(service, "com.tencent.mobileqq:id/inputBar");
            if (jinyan != null) {
                if (jinyan.getChild(0).getText().toString().trim().equals("全员禁言中")) {
                    Utils.pressBack(service);
                    Utils.sleep(2000);
                    index++;
                    massGroupMsg(messageEntityList, taskEntry);
                } else {
                    LogUtils.logInfo("没有  全员禁言中");
                    int msgLength = 0;
                    if (messageEntityList.size() > 0) {
                        MessageEntity messageEntity = messageEntityList.get(msgLength);
                        AccessibilityNodeInfo title = Utils.findViewById(service, "com.tencent.mobileqq:id/title");
                        String groupName = title.getText().toString().trim();
                        switch (messageEntity.getType()) {
                            case SEND_PHOTO:
                                LogUtils.logInfo("發送圖片  url=" + messageEntity.getImgUrl());
                                sendAndDownloadImg(taskEntry.getWx_sign(), messageEntityList, messageEntity, taskEntry, groupName);
                                break;

                            case SEND_TEXT:
                                LogUtils.logInfo("發送文本");
                                List<AccessibilityNodeInfo> list = Utils.findViewListByType(service, RelativeLayout.class.getName());
                                LogUtils.logInfo("group   %%%%%%%%------%%%%%%%" + list.get(list.size() - 2).getChild(list.get(list.size() - 2).getChildCount() - 1).toString());

                                Utils.sleep(2000);
                                sendTextMsg(taskEntry.getWx_sign(), messageEntity.getSc_id(), 1, groupName, messageEntity.getText());
                                massGroupMsg(messageEntityList, taskEntry);
                                break;
                        }
                    }
                }
            }
        }
    }

    int size = 0;
    private void sendAndDownloadImg(String wx_sign, List<MessageEntity> messageEntityList, MessageEntity messageEntity, TaskEntry taskEntry, String groupName) {
        if (!TextUtils.isEmpty(messageEntity.getImgUrl())) {
            List<String> datas = new ArrayList<>();
            String[] imgUrls = messageEntity.getImgUrl().split("@@@");
            size = imgUrls.length;
            LogUtils.logInfo("   imgUrls.length= " + imgUrls.length);
            List<String> urlList = Arrays.asList(imgUrls);
            Utils.sleep(2000);
            downloadImage(wx_sign, messageEntityList, messageEntity, taskEntry, datas, urlList, groupName, 0);
        }
    }

    private void downloadImage(final String wx_sign, final List<MessageEntity> messageEntityList, final MessageEntity messageEntity,
                               final TaskEntry taskEntry, final List<String> datas, final List<String> urlList, final String groupName, final int exponent) {
        if (exponent < urlList.size()) {
            String url = urlList.get(exponent);
            Utils.delImageToPhoto();
            LogUtils.logInfo("  下载图片 ");
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
                    downloadImage(wx_sign, messageEntityList, messageEntity, taskEntry, datas, urlList, groupName, exponent + 1);
                }
            });
        } else {
            sendPhoto(messageEntityList, taskEntry, wx_sign, messageEntity.getSc_id(), 1, groupName, datas);
            Utils.sleep(2000);
            sendNum++;
            massGroupMsg(messageEntityList, taskEntry);
        }
    }

    /***
     * 发送图片
     * @param datas
     */
    private void sendPhoto(List<MessageEntity> messageEntities, TaskEntry taskEntry, String wx_sign, String sc_id, int type, String name, List<String> datas) {
        LogUtils.logInfo("     发送图片     datas= " + datas.size());
        if (datas.size() > 0) {
            String phonetype = Utils.getSystemModel();
            AccessibilityNodeInfo groupChat = Utils.findViewByDesc(service, "群资料卡");
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
                        massGroupMsg(messageEntities, taskEntry);
                    }
                }
            }
            AccessibilityNodeInfo camera = Utils.findViewByText(service, "Camera");
            AccessibilityNodeInfo photo = Utils.findViewByText(service, "QQPhoto");
            if (camera != null) {
                Utils.clickComponeByXY(camera);
                Utils.sleep(4000);
                sendPhotoMsg(wx_sign, sc_id, type, name, datas);
            } else if (photo != null) {
                Utils.clickComponeByXY(photo);
                Utils.sleep(4000);
                sendPhotoMsg(wx_sign, sc_id, type, name, datas);
            } else {
                Utils.clickCompone(Utils.findViewByText(service, "取消"));//com.tencent.mobileqq:id/ivTitleBtnRightText
                Utils.sleep(2000);

                Utils.pressBack(service);
                Utils.sleep(2000);
            }
        }
    }

    private void sendPhotoMsg(String sign, String sc_id, int type, String name, List<String> datas) {
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
                Utils.sleep(1500);
            }
            if (Utils.findViewByTextMatch(service, "无法上传") != null) {
                Utils.findViewByText(service, "我知道了");
                Utils.sleep(2000L);

                Utils.clickCompone(Utils.findViewByText(service, "取消"));//com.tencent.mobileqq:id/ivTitleBtnRightText
                Utils.sleep(2000);

                Utils.pressBack(service);
                Utils.sleep(2000);
            } else {
                getGroupInfo(sign, sc_id, type, name, datas);
            }
        }
    }

    private void getGroupInfo(String sign, String sc_id, int type, String name, List<String> datas) {
        String groupName = null;
        String groupId = null;
        AccessibilityNodeInfo groupChat = Utils.findViewByDesc(service, "群资料卡");
        if (groupChat != null) {
            LogUtils.logInfo(" 與群聊天 ");
            Utils.clickCompone(groupChat);
            Utils.sleep(2000);

            List<AccessibilityNodeInfo> list = Utils.findViewListByType(service, TextView.class.getName());
            if (list.size() > 1) {
                if ((!TextUtils.isEmpty(list.get(0).getText())) && (!TextUtils.isEmpty(list.get(1).getText()))) {
                    groupName = list.get(0).getText().toString();
                    groupId = list.get(1).getText().toString();
                }
            }

            List<AccessibilityNodeInfo> comBtns = Utils.findViewListByType(service, CompoundButton.class.getName());
            if (comBtns.size() > 0) {
                AccessibilityNodeInfo comBtn = comBtns.get(0);
                if (!comBtn.isChecked()) {
                    Rect rect = new Rect();
                    comBtn.getBoundsInScreen(rect);
                    LogUtils.logError("x1:" + rect.left + "y1:" + rect.top + "x2:" + rect.right + "y2:" + rect.bottom);
                    int x = (rect.left + rect.right) / 2;
                    int y = (rect.top + rect.bottom) / 2;
                    Utils.tapScreenXY(x + " " + y);
                    Utils.sleep(4000L);
                }
                AccessibilityNodeInfo textBtn = null;
                if ((textBtn = Utils.findViewByTextMatch(service, "群消息提示设置")) != null) {
                    Utils.clickCompone(textBtn);
                    Utils.sleep(2000L);

                    if (Utils.findViewById(service, "com.tencent.mobileqq:id/action_sheet_actionView") != null) {
                        Utils.sleep(2000);
                        if ((textBtn = Utils.findViewByText(service, "屏蔽群消息")) != null) {
                            Utils.clickCompone(textBtn);
                            Utils.sleep(3000L);
                        }
                    }
                }
            }
            //android.widget.CompoundButton
        }
        FileUtils.deleteFiles(datas);
        LogUtils.logInfo("返回到消息页");
        Utils.pressBack(service);
        Utils.sleep(3000);

        if (Utils.isTragetActivity(Constants.QQ_CHATSETTING)) {
            Utils.pressBack(service);
            Utils.sleep(3000);
        }

        if (Utils.findViewByDesc(service, "群资料卡") != null) {
            Utils.clickCompone(Utils.findViewByDesc(service, "返回消息"));
            Utils.sleep(3000L);
        }
        if (!TextUtils.isEmpty(groupName)) {
            name = groupName;
        }
        addDataToDB(type, sign, name, groupId);
        HttpHandler.qunfaFinish(sign, sc_id, groupId);
    }

    /***
     *  输入文本并发送
     * @param sendmsg
     */
    private void sendTextMsg(String sign, String sc_id, int type, String name, String sendmsg) {
        if (!TextUtils.isEmpty(sendmsg)) {
            AccessibilityNodeInfo editText = Utils.findViewByType(service, EditText.class.getName());
            if (editText != null) {
                Utils.componeFocus(editText);
                Utils.sleep(2000L);
                Utils.selectAllText(editText);
                Utils.sleep(2000L);
                Utils.inputText(service, editText, sendmsg);
                Utils.sleep(2000L);
                Utils.clickCompone(Utils.findViewById(service, "com.tencent.mobileqq:id/fun_btn"));
                Utils.sleep(3000L);
                Utils.pressBack(service);
                Utils.sleep(2000L);
                getGroupInfo(sign, sc_id, type, name, null);
            } else {
                Utils.pressBack(service);
                Utils.sleep(2000);
            }
        } else {
            Utils.pressBack(service);
            Utils.sleep(2000);
        }
    }

    private void addDataToDB(int type, String wxsign, String groupName, String groupId) {
        String name = Utils.getBASE64(groupName);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String date = formatter.format(curDate);
        LogUtils.logInfo("存入数据库  " + groupName + "    " + groupId + "   " + date);
        ReplyMsgInfo replyMsgInfo = new ReplyMsgInfo();
        replyMsgInfo.setGroupName(name);
        replyMsgInfo.setSendmsgTime(date);
        replyMsgInfo.setMsgType(type);
        replyMsgInfo.setGroupId(groupId);
        replyMsgInfo.setWxsign(wxsign);
        List<ReplyMsgInfo> replyMsgInfos = DataSupport.where("wxsign= ? and groupId = ?", wxsign, groupId).find(ReplyMsgInfo.class);
        if (replyMsgInfos.size() != 0) {
            int id = replyMsgInfos.get(0).getId();
            ContentValues values = new ContentValues();
            values.put("sendmsgTime", date);
            values.put("groupName", name);
            DataSupport.update(ReplyMsgInfo.class, values, id);
        } else {
            replyMsgInfo.saveThrows();
        }
    }

}
