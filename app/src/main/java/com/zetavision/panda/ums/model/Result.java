package com.zetavision.panda.ums.model;

import com.google.gson.Gson;
import com.zetavision.panda.ums.Utils.MyParameterizedType;

import java.lang.reflect.Type;
import java.util.List;

public class Result {
    private int returnCode;
    private String returnMessage;
    private String returnData;

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }

    public String getReturnData() {
        return returnData;
    }

    public void setReturnData(String returnData) {
        this.returnData = returnData;
    }

    public <T> T getData(Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(getReturnData(), clazz);
    }

    public <T> List<T> getList(Class<T> clazz) {
        Gson gson = new Gson();
        // 生成List<T> 中的 List<T>
        Type type = new MyParameterizedType(List.class, new Class[]{clazz});
        // 根据List<T>生成完整的Result<List<T>>
        return gson.fromJson(getReturnData(), type);
    }
}
