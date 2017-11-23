package com.cc.helperqq.entity;

import org.litepal.crud.DataSupport;

/**
 * Created by fangying on 2017/11/16.
 */

public class GroupMembersInfo extends DataSupport{

    private int id;
    private String membersName;// 成员昵称
    private String memberId; // 成员id
    private String groupId; // 所在群id
    private String membersSex;
    private String groupCard; // 群名片
    private String groupName;
    private String wxsign;
    private String sendOrNot; // 是否已发送过

    private GroupInfo groupInfo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMembersName() {
        return membersName;
    }

    public void setMembersName(String membersName) {
        this.membersName = membersName;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMembersSex() {
        return membersSex;
    }

    public void setMembersSex(String membersSex) {
        this.membersSex = membersSex;
    }

    public String getGroupCard() {
        return groupCard;
    }

    public void setGroupCard(String groupCard) {
        this.groupCard = groupCard;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getWxsign() {
        return wxsign;
    }

    public void setWxsign(String wxsign) {
        this.wxsign = wxsign;
    }

    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }

    public String getSendOrNot() {
        return sendOrNot;
    }

    public void setSendOrNot(String sendOrNot) {
        this.sendOrNot = sendOrNot;
    }
}
