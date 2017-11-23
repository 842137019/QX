package com.cc.helperqq.task;

import android.os.Handler;
import android.os.Message;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsListView;
import android.widget.LinearLayout;

import com.cc.helperqq.entity.NewsEntry;
import com.cc.helperqq.utils.Constants;
import com.cc.helperqq.utils.Utils;
import com.cc.helperqq.service.HelperQQService;

/**
 * 瀏覽新聞
 */
public class BrowseNewsTask {

    private HelperQQService service;

    private Handler handler;

    private int findCount = 0;

    private AccessibilityNodeInfo newsItem;

    private Handler currentHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Object obj = msg.obj;
                if (obj != null && obj instanceof NewsEntry) {
                    NewsEntry newsEntry = (NewsEntry) obj;
                    traversalNews(newsEntry);
                }
            }
        }
    };

    public BrowseNewsTask(HelperQQService service, Handler handler) {
        this.service = service;
        this.handler = handler;
    }

//    public void browseNews() {
//        Utils.startPage(Constants.QQ_HOME_ACTIVITY);
//        newsItem = null;
//        LogUtils.logError("点击消息");
//        AccessibilityNodeInfo wxBtn = Utils.findViewByTextMatch(service, "消息");
//        if (wxBtn == null) {
//            handler.sendEmptyMessage(1);
//            return;
//        }
//        Utils.clickCompone(wxBtn);
//        Utils.sleep(3 * 1000L);
//        findNewsContent();
//        if (newsItem != null) {
//            LogUtils.logError("点击腾讯新闻 ");
//            Utils.clickCompone(newsItem);
//            Utils.sleep(5 * 1000L);
//            exeReadNews();
//        } else {
//            Utils.sleep(3 * 1000L);
//            handler.sendEmptyMessage(1);
//        }
//    }

//    private void findNewsContent() {
//        LogUtils.logInfo(" 查找腾讯新闻 ");
//        List<AccessibilityNodeInfo> texts = Utils.findViewListById(service, Constants.WX_LIST_ITEM_ID);
//        if (texts != null && texts.size() > 0) {
//            for (int i = 0; i < texts.size(); i++) {
//                AccessibilityNodeInfo childView = texts.get(i);
//                LogUtils.logInfo(" i= "+i + "   "+texts.get(i).toString());
//               // LogUtils.logError(" browse  " + childView.getText().toString());
//                //adb shell am start -n com.tencent.mobileqq/.activity.ChatActivity
//                if (!TextUtils.isEmpty(childView.getText()) && childView.getText().toString().equals("腾讯新闻")) {
//                    newsItem = childView;
//                    return;
//                } else {
//                    childView.recycle();
//                }
//            }
//            if (findCount < 3) {
//                scrollConcatList();
//                Utils.sleep(3 * 1000L);
//                findNewsContent();
//            } else {
//                resetListView(service);
//                return;
//            }
//        }
//    }

//    private void scrollConcatList() {
//        AccessibilityNodeInfo listView = Utils.findViewById(service, Constants.WX_LIST_ID);
//        if (listView != null) {
//            Utils.scrollViewDown(listView);
//            Utils.sleep(3000L);
//            listView.recycle();
//        }
//        findCount += 1;
//    }

    private void resetListView(HelperQQService service) {
        AccessibilityNodeInfo listView = Utils.findViewById(service, Constants.WX_LIST_ID);
        if (listView != null) {
            for (int x = 0; x < findCount; x++) {
                Utils.scrollViewUp(listView);
                Utils.sleep(3000L);
            }
            listView.recycle();
        }
        findCount = 0;
    }

    public void exeReadNews() {
        Utils.pressScrollDown();
        Utils.sleep(5000L);
        AccessibilityNodeInfo listView = Utils.findViewByType(service, AbsListView.class.getName());
        if (listView != null && listView.getChildCount() > 0) {
            AccessibilityNodeInfo lastChld = listView.getChild(listView.getChildCount() - 1);
            if (lastChld != null) {
                NewsEntry newsEntry = new NewsEntry(lastChld, 0);
                traversalNews(newsEntry);
            } else {
                listView.recycle();
                Utils.clickCompone(Utils.findViewByDesc(service, "返回"));
                Utils.sleep(3 * 1000L);
//                handler.sendEmptyMessage(1);
            }
        } else {
            Utils.clickCompone(Utils.findViewByDesc(service, "返回"));
            Utils.sleep(3 * 1000L);
//            handler.sendEmptyMessage(1);
        }
    }

    private void traversalNews(NewsEntry newsEntry) {
        AccessibilityNodeInfo lastChld = newsEntry.getLastChld();
        int index = newsEntry.getIndex();
        if (index < lastChld.getChildCount()) {
            AccessibilityNodeInfo finalView = newsEntry.getLastChld().getChild(index);
            if (finalView != null && finalView.getClassName().equals(LinearLayout.class.getName())) {
                Utils.clickComponeByXY(finalView);
                Utils.sleep(5 * 1000L);
                if (Utils.isTragetActivity(Constants.PAGE_NEWS_CONTENT_PAGE)) {
                    AccessibilityNodeInfo goonBtn = Utils.findViewByTextMatch(service, "继续");
                    if (goonBtn != null) {
                        Utils.clickCompone(goonBtn);
                        Utils.sleep(3 * 1000L);
                    }
                    new ScrollNewThread(newsEntry).start();
                } else {
                    newsEntry.setIndex(index + 1);
                    traversalNews(newsEntry);
                }
            } else {
                newsEntry.setIndex(index + 1);
                traversalNews(newsEntry);
            }
        } else {
            Utils.sleep(3 * 1000L);
            Utils.clickCompone(Utils.findViewByDesc(service, "返回"));
            Utils.sleep(3 * 1000L);
            resetListView(service);
            Utils.sleep(3 * 1000L);
//            handler.sendEmptyMessage(1);
        }
    }

    private class ScrollNewThread extends Thread {

        private NewsEntry newsEntry;

        public ScrollNewThread(NewsEntry newsEntry) {
            this.newsEntry = newsEntry;
        }

        @Override
        public void run() {
            Utils.pressScrollDown();
            Utils.sleep(4 * 1000L);
            Utils.pressScrollDown();
            Utils.sleep(4 * 1000L);
            Utils.pressScrollDown();
            Utils.sleep(4 * 1000L);
            Utils.pressBack(service);
            Utils.sleep(3 * 1000L);

            newsEntry.setIndex(newsEntry.getIndex() + 1);
            Message msg = currentHandler.obtainMessage();
            msg.obj = newsEntry;
            msg.what = 1;
            msg.sendToTarget();
        }
    }
}
