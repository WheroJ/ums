package com.zetavision.panda.ums.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.zetavision.panda.ums.model.User;

import java.util.Locale;


public class UserPreferences {
    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }

    static {
        mContext = UIUtils.getContext();
    }

    public void setLanguage(String name) {
        SPUtil.saveString(Constant.EVENT_REFRESH_LANGUAGE, name);
    }

    public String getLanguage() {
        return SPUtil.getString(Constant.EVENT_REFRESH_LANGUAGE, Locale.CHINESE.getLanguage());
    }

    public void setCookie(String host, String cookies) {
        SPUtil.saveString(host, cookies);
    }

    public String getCookie(String host) {
        return SPUtil.getString(host,"[]");
    }

    public void clearCookie() {
        SPUtil.clearCookie();
    }

    public void saveUser(User user) {
        Gson gson = new Gson();
        SPUtil.saveString(Constant.EVENT_REFRESH_USER, gson.toJson(user));
    }

    public User getUser() {
        Gson gson = new Gson();
        return gson.fromJson(SPUtil.getString(Constant.EVENT_REFRESH_USER, null), User.class);
    }
}
