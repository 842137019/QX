package com.cc.helperqq.service.hook;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;


import com.cc.helperqq.entity.TaskEntry;
import com.cc.helperqq.utils.FileUtils;
import com.cc.helperqq.utils.HookUtils;
import com.cc.helperqq.utils.LogUtils;
import com.cc.helperqq.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.getObjectField;

/**
 * Created by Administrator on 2017/8/23.
 */

public class HelperService implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.log("  *****************************--------------- ");
        hookSnak(loadPackageParam);
        hookPhoneInfo(loadPackageParam);
        discussionMemberInfo(loadPackageParam);//成员信息
        openTroopAction(loadPackageParam);//打开群的intent
        HookUtils.HookAndChange(loadPackageParam.classLoader);

    }

    private void hookIntent(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Bundle b;
        XposedHelpers.findAndHookMethod("android.os.Bundle", loadPackageParam.classLoader, "putString", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object k = param.args[0];
                Object v = param.args[1];
                if (k instanceof String) {
                    String kText = (String) k;
                    LogUtils.logInfo("kText=" + kText);
                }
                if (v instanceof String) {
                    String vText = (String) v;
                    LogUtils.logInfo("vText=" + vText);
                }
            }
        });
    }

    private void hookSnak(XC_LoadPackage.LoadPackageParam paramLoadPackageParam) throws Throwable {
        try {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("android.hardware.SystemSensorManager$SensorEventQueue", paramLoadPackageParam.classLoader), "dispatchSensorEvent", new XC_MethodHook() {
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam paramAnonymousMethodHookParam) throws Throwable {
                    Boolean isShake = false;
                    Object obj = Utils.getProperty("shnak");
                    if (obj != null) {
                        isShake = Boolean.valueOf(obj.toString());
                    }

                    if (isShake) {
                        ((float[]) paramAnonymousMethodHookParam.args[1])[0] = (Utils.getRandom().nextFloat() * 1200.0F + 125.0F);
                        Utils.setProperty("shnak", false);
                    }
                }
            });
        } catch (Throwable e) {
            throw e;
        }
    }

    private void hookPhoneInfo(XC_LoadPackage.LoadPackageParam paramLoadPackageParam) throws Throwable {
        try {
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", paramLoadPackageParam.classLoader, "getDeviceId", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object obj = Utils.getObject(TaskEntry.class.getSimpleName());
                    if (obj != null) {
                        TaskEntry taskEntry = (TaskEntry) obj;
                        XposedBridge.log(" askEntry.getImei()   " + taskEntry.getImei());
                        param.setResult(taskEntry.getImei());
                    }
                }
            });

            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", paramLoadPackageParam.classLoader, "getSubscriberId", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object obj = Utils.getObject(TaskEntry.class.getSimpleName());
                    if (obj != null) {
                        TaskEntry taskEntry = (TaskEntry) obj;
                        param.setResult(taskEntry.getSid());
                    }
                }
            });

            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", paramLoadPackageParam.classLoader, "getMacAddress", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object obj = Utils.getObject(TaskEntry.class.getSimpleName());
                    if (obj != null) {
                        TaskEntry taskEntry = (TaskEntry) obj;
                        param.setResult(taskEntry.getMac());
                    }
                }
            });

        } catch (Throwable e) {
            throw e;
        }
    }

    private void hookTroopMemberList(XC_LoadPackage.LoadPackageParam loadPackageParam) {

        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.TroopMemberListActivity$ListAdapter", loadPackageParam.classLoader, "a", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                ContentValues cv = (ContentValues) param.args[2];
                XposedBridge.log(" --------" + cv.toString());
                List<Object> listObj = (List<Object>) getObjectField(param.thisObject, "fHE");
                XposedBridge.log(" --------" + listObj.size());


            }
        });


        //com.tencent.mobileqq.activity.TroopMemberListActivity$ListAdapter
        XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.tencent.mobileqq.activity.TroopMemberListActivity$ListAdapter", loadPackageParam.classLoader),
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Class self = param.getClass();
                        XposedBridge.log(" --------" + self.getSimpleName());

                        Method[] mothod = self.getMethods();
                        XposedBridge.log(" --------" + mothod.length);
/*                        // 获取对象的所有属性
                        Field[] fields = entityClass.getDeclaredFields();

                        // 获取对象的所有方法
                        Method[] methods = entityClass.getDeclaredMethods();
                        List<String> listfeld = new ArrayList<>();
                        for (Field field : fields) {
                            System.out.println("属性名称有：" + field.getName());

                        }
                        for (Method method : methods) {
                            System.out.println("方法名称有：" + method.getName());
                        }*/
                    }
                }
        );
    }

    private void discussionMemberInfo(XC_LoadPackage.LoadPackageParam loadPackageParam) throws ClassNotFoundException {
        XposedHelpers.findClass("java.util.Map", loadPackageParam.classLoader);
        Class<?> TroopMemberInfo = XposedHelpers.findClass("com.tencent.mobileqq.data.TroopMemberInfo", loadPackageParam.classLoader);//loadPackageParam.classLoader.loadClass("com.tencent.mobileqq.data.TroopMemberInfo");
        Class<?> FriendsManager1 = XposedHelpers.findClass("com.tencent.mobileqq.app.FriendsManager", loadPackageParam.classLoader);//; loadPackageParam.classLoader.loadClass("com.tencent.mobileqq.app.FriendsManager");
        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.TroopMemberListActivity", loadPackageParam.classLoader, "a", TroopMemberInfo, FriendsManager1, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                        /* active_point.setAccessible(true);// 设置操作权限为true
                        for (Field field : fs) {
                        field.setAccessible(true);// 设置操作权限为true
                        sb = sb + field.getName() + ":" + field.get(o) + "  ";
                    } */
                Object obj = Utils.getObject(TaskEntry.class.getSimpleName());
                TaskEntry taskEntry = null;
                if (obj != null) {
                    taskEntry = (TaskEntry) obj;
                }
                Object o = param.args[0];
                if (o != null) {
                    Class cls = o.getClass();
                    //  Field[] fs = cls.getDeclaredFields();
                    StringBuffer sb = new StringBuffer();
                    sb.append("{");
                    Field friendnick = cls.getDeclaredField("friendnick");
                    friendnick.setAccessible(true);
                    sb.append("\"" + friendnick.getName() + "\"" + ":" + "\"" + Utils.getBASE64(friendnick.get(o)+"") + "\",");

                    Field memberuin = cls.getDeclaredField("memberuin");
                    memberuin.setAccessible(true);
                    sb.append("\"" + memberuin.getName() + "\"" + ":" + "\"" + memberuin.get(o) + "\",");

                    Field troopuin = cls.getDeclaredField("troopuin");
                    troopuin.setAccessible(true);
                    sb.append("\"" + troopuin.getName() + "\"" + ":" + "\"" + troopuin.get(o) + "\",");

                    Field troopnick = cls.getDeclaredField("troopnick");
                    troopnick.setAccessible(true);
                    sb.append("\"" + troopnick.getName() + "\"" + ":" + "\"" + Utils.getBASE64(troopnick.get(o)+"") + "\",");

                    Field sex = cls.getDeclaredField("sex");
                    sex.setAccessible(true);
                    sb.append("\"" + sex.getName() + "\"" + ":" + "\"" + sex.get(o) + "\"");

                    sb.append("},");
                    FileUtils.writeFileToSDCard(sb.toString(), "GroupMembers", taskEntry.getWx_sign() + ".txt", true, true);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    private void openTroopAction(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.TroopMemberListActivity", loadPackageParam.classLoader, "a", Context.class, String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Object o = param.args[1];
                Object obj = param.args[2];
                if (o != null) {
                    String paramString = (String) o;
                    //  FileUtils.writeFileToSDCard("Intent-String:"+paramString, "/sdcard/Financail","Exception_Log.txt",true,true);
                }
                if (obj != null) {
                    int paramInt = (int) obj;
                    // FileUtils.writeFileToSDCard("Intent-int:"+paramInt, "/sdcard/Financail","Exception_Log.txt",true,true);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

}
