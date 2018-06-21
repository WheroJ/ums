package com.zetavision.panda.ums.model;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;

/**
 * Created by wheroj on 2018/1/31 16:49.
 *
 * @describe
 */

public class FormInfoDetail extends DataSupport{
    public String formId;

    /**
     * 设备code
     */
    public String equipmentCode;

    /**
     * 点检路线
     */
    public String inspectRouteCode;

    public String actionType;
    public int utilitySystemId;

    public static int SYNC = 2;
    public static int UNSYNC = 1;
    /**
     * 是否上传，默认为已经和服务器同步   1:还未同步   2:已经同步
     */
    public int isUpload = FormInfo.DONE;

    public FormInfo form;
    public ArrayList<FormItem> formItemList;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FormInfo) {
            return ((FormInfoDetail) obj).formId.equals(formId);
        }
        return super.equals(obj);
    }
}
