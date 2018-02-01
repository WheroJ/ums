package com.zetavision.panda.ums.utils;

/**
 * Created by developer on 2017/12/11.
 */

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

public class ActivityCollector {
    //声明一个List集
    public static List<Activity> mActivities = new LinkedList<>();

    //将activity添加到List集中
    public static void addActivity(Activity activity){
        synchronized (mActivities) {
            mActivities.add(activity);
        }
    }

    //将某一个Activity移除
    public static void removeActivity(Activity activity){
        synchronized (mActivities) {
            mActivities.remove(activity);
        }
    }

    //结束所有添加进来的的Activity
    public static void finishAll(){
        List<Activity> clone;
        synchronized (mActivities) {
            clone = new LinkedList<>(mActivities);
        }

        for(Activity activity:clone){
            //如果activity没有销毁，那么销毁
            if(!activity.isFinishing()){
                activity.finish();
                mActivities.remove(activity);
            }
        }
    }
}
