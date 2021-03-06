package com.zetavision.panda.ums.model;

import android.text.TextUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;

/**
 * Created by wheroj on 2018/1/31 17:01.
 *
 * @describe
 */

public class FormItem extends DataSupport{
    public String formId;
    public String formItemId;
    public String seq;//1",
    public String basicActionCode;//M-11001-001",
    public String basicActionName;//M-11001-001",
    public String description;//",
    public String valueType;//N",
    public String presetValue;//10",
    public String result = "";
    public String unit;//",
    public String lowerLimit;//5",
    public String upperLimit;//10",
    public String optionValues;//"
    public String remarks;//"
    public String equipmentName;

    /**
     * 是否开机
     */
    public String isStartingUp;

    /**
     * 是否需要需要随机牌照  only点检表单
     * 取值为：  Y   N
     */
    public String photoMust;

    public final static String TYPE_Y = "Y";

    /**
     * 点检特有
     */
    public String optionValuesDescription;//",
    public String equipmentSeq;//1",
    public String equipmentCode;//4CA-CA-CAC01",
    public String inspectFormItemId;//10010",
    public String equipmentId;//11003",
    public String inspectFormId;//10020",
    public String actionSeq;//1",

    public String inspectResult;//",

    public String hadTakePhoto;
    public ArrayList<String> photoUrls;

    public ArrayList<String> photoPaths;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FormItem) {
            if (formItemId != null) {
                return formItemId.equals(((FormItem) obj).formItemId);
            } else if (equipmentId != null) {
                return equipmentId.equals(((FormItem) obj).equipmentId);
            } else return super.equals(obj);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(result)) {
            builder.append(result.hashCode());
        }
        if (!TextUtils.isEmpty(remarks)) {
            builder.append(remarks.hashCode());
        }
        if (photoPaths != null) {
            builder.append(photoPaths.hashCode());
        }
        if (photoUrls != null) {
            builder.append(photoUrls.hashCode());
        }

        builder.append(isStartingUp);
        return builder.toString().hashCode();
    }
}
