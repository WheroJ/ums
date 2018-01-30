package com.zetavision.panda.ums.Utils;

import android.content.Context;
import android.content.SharedPreferences;

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
        SharedPreferences preferences = this.mContext.getSharedPreferences("default", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constant.EVENT_REFRESH_LANGUAGE, name);
        editor.apply();
    }

    public String getLanguage() {
        SharedPreferences preferences = this.mContext.getSharedPreferences("default", Context.MODE_PRIVATE);
        return preferences.getString(Constant.EVENT_REFRESH_LANGUAGE, Locale.CHINESE.getLanguage());
    }

    public void setCookie(String host, String cookies) {
        SharedPreferences preferences = this.mContext.getSharedPreferences("cookie", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(host, cookies);
        editor.apply();
    }

    public String getCookie(String host) {
        SharedPreferences preferences = this.mContext.getSharedPreferences("cookie", Context.MODE_PRIVATE);
        return preferences.getString(host,"[]");
    }

    public void clearCookie() {
        SharedPreferences preferences = this.mContext.getSharedPreferences("cookie", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    public void saveUser(User user) {
        SharedPreferences preferences = this.mContext.getSharedPreferences("default", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        editor.putString(Constant.EVENT_REFRESH_USER, gson.toJson(user));
        editor.apply();
    }

    public User getUser() {
        SharedPreferences preferences = this.mContext.getSharedPreferences("default", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        return gson.fromJson(preferences.getString(Constant.EVENT_REFRESH_USER, null), User.class);
    }

}
