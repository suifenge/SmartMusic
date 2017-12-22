package com.suifeng.library.base.ui;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * App activity的管理类
 */
public class BaseAppManager {

    private static final String TAG = BaseAppManager.class.getSimpleName();

    private static BaseAppManager instance = null;
    private static List<Activity> mActivities = new LinkedList<Activity>();

    private BaseAppManager() {

    }

    public static BaseAppManager getInstance() {
        if (null == instance) {
            synchronized (BaseAppManager.class) {
                if (null == instance) {
                    instance = new BaseAppManager();
                }
            }
        }
        return instance;
    }

    public int size() {
        return mActivities.size();
    }

    public synchronized Activity getForwardActivity() {
        return size() > 0 ? mActivities.get(size() - 1) : null;
    }

    public synchronized void addActivity(Activity activity) {
        mActivities.add(activity);
    }

    public synchronized void removeActivity(Activity activity) {
        if (mActivities.contains(activity)) {
            mActivities.remove(activity);
        }
    }

    /**
     * 结束指定的Activity
     * @param activity
     */
    public synchronized void finishActivity(Activity activity) {
        if (activity != null && mActivities.contains(activity)) {
            mActivities.remove(activity);
            activity.finish();
        }
    }

    public synchronized void clear() {
        for (int i = mActivities.size() - 1; i > -1; i--) {
            Activity activity = mActivities.get(i);
            removeActivity(activity);
            activity.finish();
            i = mActivities.size();
        }
    }

    /**
     * 应用退出，结束所有的activity
     * @param complete 是否完全退出杀死进程
     */
    public void exit(boolean complete) {
        clear();
        if(complete) {
            System.exit(0);
        }
    }

    public synchronized void clearToTop() {
        for (int i = mActivities.size() - 2; i > -1; i--) {
            Activity activity = mActivities.get(i);
            removeActivity(activity);
            activity.finish();
            i = mActivities.size() - 1;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public synchronized void finishActivityclass(Class<?> cls) {
        if (mActivities != null) {
            for (Activity activity : mActivities) {
                if (activity.getClass().equals(cls)) {
                    mActivities.remove(activity);
                    finishActivity(activity);
                    break;
                }
            }
        }

    }
}
