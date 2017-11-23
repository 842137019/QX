package com.cc.helperqq.task;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;

import com.cc.helperqq.entity.TaskEntry;
import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.EventBusUtils;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.QQError;
import com.cc.helperqq.utils.Utils;
import com.cc.helperqq.service.HelperQQService;

import java.util.List;


/**
 * Created by fangying on 2017/8/30.
 */

public class TaskController {

    private HelperQQService service;
    private AddGroupTask addGroupTask;
    private AddPeopleTask addPeopleTask;
    private ModifyInfoTask modifyInfoTask;
    private ReductionTask reductionTask;
    private BackupsTask backupsTask;
    private ReplyMessageTask messageTask;
    private MassMessageTask massMessageTask;
    private CloseMsgNoticeTask noticeTask;
    private BrowseNewsTask newsTask;
    private ClearChatRecordTask clearChatRecordTask;
    private GroupMembersTask groupMembersTask;
    private CreateMultiplayerChatTask createMultiplayerChatTask;
    private CloseFunctionTask closeFunctionTask;
    private GroupMembersSendMessagesTask membersSendMessagesTask;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //TODO 执行下个分任务
                    LogUtils.logInfo("执行下个分任务");
                    EventBusUtils.getInstance().sendMessage("taskRun", new Boolean(false));
                    break;
                case 2:
                    //TODO 账号错误，执行下一个任务
                    EventBusUtils.getInstance().sendMessage("taskRun", new Boolean(true));
                    break;
            }
        }
    };

    public TaskController(HelperQQService service) {
        this.service = service;
        addGroupTask = new AddGroupTask(service, handler);
        addPeopleTask = new AddPeopleTask(service, handler);
        modifyInfoTask = new ModifyInfoTask(service, handler);
        reductionTask = new ReductionTask(service, handler);
        backupsTask = new BackupsTask(service, handler);
        messageTask = new ReplyMessageTask(service, handler);
        noticeTask = new CloseMsgNoticeTask(service, handler);
        newsTask = new BrowseNewsTask(service, handler);
        massMessageTask = new MassMessageTask(service, handler);
        clearChatRecordTask = new ClearChatRecordTask(service, handler);
        groupMembersTask = new GroupMembersTask(service, handler);
        createMultiplayerChatTask = new CreateMultiplayerChatTask(service, handler);
        closeFunctionTask = new CloseFunctionTask(service, handler);
        membersSendMessagesTask = new GroupMembersSendMessagesTask(service, handler);
    }

    public void executeTask(String type) {
        if (TextUtils.isEmpty(type)) {
            LogUtils.logInfo("  ---------------  ");
            handler.sendEmptyMessage(2);
            return;
        }
        Object obj = Utils.getObject(TaskEntry.class.getSimpleName());
        LogUtils.logInfo("  *******  obj ******  ");
        if (obj != null) {
            TaskEntry taskEntry = (TaskEntry) obj;
            if (QQError.validate(service, taskEntry)) {
                LogUtils.logInfo("  ****  taskEntry  *****  " + taskEntry.toString());
                QQError.loginPromptAcitvity(service);
                LogUtils.logInfo("  *******   ******  ");
                assignments(Integer.valueOf(type), taskEntry);
            } else {
                if (Utils.wxIsRun()) {
                    Utils.exitApp(Constants.APP_NAME);
                    Utils.sleep(5000L);
                }
                handler.sendEmptyMessage(2);
            }
        } else {
            handler.sendEmptyMessage(2);
        }
    }

    private void assignments(int type, TaskEntry taskEntry) {
        LogUtils.logInfo("任务id = " + type);
        switch (type) {
            case Constants.TYPE_MODIFYINFO_TASK:
                LogUtils.logInfo("修改个人信息");
                modifyInfoTask.modifyInfo(taskEntry);
                break;
//            case Constants.TYPE_BROWS_ENEWS_TASK:
//                LogUtils.logInfo("浏览新闻");
//                newsTask.browseNews();
//                break;

            case Constants.TYPE_ADD_PEOPLE_TASK:
                LogUtils.logInfo(" 添加好友 ");
                addPeopleTask.addContacts(taskEntry);
                break;

            case Constants.TYPE_ADD_GROUP_TASK:
                LogUtils.logInfo(" 添加群組 ");
                addGroupTask.addGroup(taskEntry);
                break;

            case Constants.TYPE_REDUCTION_TASK:
                LogUtils.logInfo("还原文件");
                reductionTask.ReductionData(taskEntry);
                break;

            case Constants.TYPE_BACKUPS_TASK:
                LogUtils.logInfo("备份文件");
                backupsTask.backups(taskEntry);
                break;

//            case Constants.TYPE_REPLY_MESSAGE_TASK:
//                LogUtils.logInfo("群发消息");
//                TestMassMsgTask testMassMsgTask = new TestMassMsgTask(service, handler);
//                testMassMsgTask.getSendMassMsgDB(taskEntry);
//                break;

            case Constants.TYPE_MASSMSG_TASK:
                LogUtils.logInfo("在QQ群内发消息");
                massMessageTask.sendTypeMsg(taskEntry);
                break;

            case Constants.TYPE_CLOSE_NOTICE_TASK:
                LogUtils.logInfo("关闭消息通知");
                noticeTask.closeNoticeGetInfo();
                break;

            case Constants.TYPE_CLEAR_CHATRECORD_TASK:
                LogUtils.logInfo("清空聊天记录");
                clearChatRecordTask.emptyRecords();
                break;
//            case 20:
//                groupMembersTask.sendTypeMsg(taskEntry);
//                break;
            case Constants.TYPE_GROUP_MEMBERS_MSG_TASK:
                LogUtils.logInfo("与群成员聊天");
                groupMembersTask.sendTypeMsg(taskEntry);
                break;
            case 33:
                LogUtils.logInfo("点击群列表");
                RegisterTask registerTask = new RegisterTask(service, handler);
                registerTask.add(taskEntry);
                break;
            case 34:
                LogUtils.logInfo("关闭通知");
                ClosePhoneNotificationTask phoneNotificationTask = new ClosePhoneNotificationTask(service, handler);
                phoneNotificationTask.setqNotifcation();
                break;

            case 36:
                LogUtils.logInfo("关闭功能");
                closeFunctionTask.closeFunction();
                break;

            case 35:
                LogUtils.logInfo("创建讨论组");
                createMultiplayerChatTask.CreateChat(taskEntry);
                break;

        }
    }
}
