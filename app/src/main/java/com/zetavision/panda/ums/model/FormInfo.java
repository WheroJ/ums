package com.zetavision.panda.ums.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.zetavision.panda.ums.utils.TimeUtils;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

public class FormInfo extends DataSupport implements Comparable{


    /**
     * 下载状态
     */
    public static final int WAIT = 1;
    public static final int PROGRESS = 2;
    public static final int DONE = 3;
    public static final int FAIL = 4;

    private int download_status = WAIT; //默认等待下载
    public int sop_download_status = WAIT;

    public static final String ACTION_TYPE_M = "M";
    public static final String ACTION_TYPE_P = "P";

    public static final String MAINT_TYPE_TBM= "TBM";
    public static final String MAINT_TYPE_CBM= "CBM";

    public int utilitySystemId;
    private String utilitySystemCode;

    /**
     * 天气
     */
    public String weather;

    /**
     * 轮班
     */
    public String shift;

    /**
     * 保养开始时间
     */
    public long startTime;

    /**
     * 保养结束时间
     */
    public long completeTime;

    /**
     * 备注
     */
    public String fillinRemarks;

    /**
     * 开始的用户
     */
    public String startUser;

    /**
     * 完成的用户
     */
    public String completeUser;

    @Column(unique = true, defaultValue = "undefine")
    private String formId;
    private String formCode;
    private String status;
    private String planDate;
    private String actionType;
    public String actionTypeDescription;
    public String sopFileName;
    public String sopUrl;
    public String sopLocalPath;

    // 保养表单
    private String equipmentCode;
    public String equipmentName;
    private String maintType;
    private String maintPeriodName;
    private String maintPeriodDescription;
    private String maintParamTypeCode;
    private String maintParamTypeDescription;

    /**
     * 保养周期流程code
     */
    public String maintFlowCode;

    // 点检表单
    private String inspectRouteCode;
    private String inspectRouteDescription;
    public String inspectRouteName;
    public String inspectPeriodCode;
    public String inspectPeriodDescription;
    public String inspectPeriodName;
    public String inspectFlowCode;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FormInfo) {
            if (formId != null) {
                return formId.equals(((FormInfo) obj).formId);
            } else return super.equals(obj);
        }
        return super.equals(obj);
    }

    // 根据表单类型获取line或者eqp
    public String getLineOrEqp(){
        if (actionType.equals(ACTION_TYPE_M)) {
            return equipmentCode;
        } else if (actionType.equals(ACTION_TYPE_P)){
            return inspectRouteCode;
        } else return null;
    }

    // 根据类型获取说明
    public String getDesc() {
        if (actionType.equals(ACTION_TYPE_M)) {
            if (maintType.equals(MAINT_TYPE_TBM)) {
                return maintPeriodDescription;
            } else {
                return maintParamTypeDescription;
            }
        } else if (actionType.equals(ACTION_TYPE_P)) {
            return inspectRouteDescription;
        } else return null;
    }

    public int getDownload_status() {
        return download_status;
    }

    public void setDownload_status(int download_status) {
        this.download_status = download_status;
    }

    public int getUtilitySystemId() {
        return utilitySystemId;
    }

    public void setUtilitySystemId(int utilitySystemId) {
        this.utilitySystemId = utilitySystemId;
    }

    public String getUtilitySystemCode() {
        return utilitySystemCode;
    }

    public void setUtilitySystemCode(String utilitySystemCode) {
        this.utilitySystemCode = utilitySystemCode;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlanDate() {
        return planDate;
    }

    public void setPlanDate(String planDate) {
        this.planDate = planDate;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    public String getMaintType() {
        return maintType;
    }

    public void setMaintType(String maintType) {
        this.maintType = maintType;
    }

    public String getMaintPeriodName() {
        return maintPeriodName;
    }

    public void setMaintPeriodName(String maintPeriodName) {
        this.maintPeriodName = maintPeriodName;
    }

    public String getMaintPeriodDescription() {
        return maintPeriodDescription;
    }

    public void setMaintPeriodDescription(String maintPeriodDescription) {
        this.maintPeriodDescription = maintPeriodDescription;
    }

    public String getMaintParamTypeCode() {
        return maintParamTypeCode;
    }

    public void setMaintParamTypeCode(String maintParamTypeCode) {
        this.maintParamTypeCode = maintParamTypeCode;
    }

    public String getMaintParamTypeDescription() {
        return maintParamTypeDescription;
    }

    public void setMaintParamTypeDescription(String maintParamTypeDescription) {
        this.maintParamTypeDescription = maintParamTypeDescription;
    }

    public String getInspectRouteCode() {
        return inspectRouteCode;
    }

    public void setInspectRouteCode(String inspectRouteCode) {
        this.inspectRouteCode = inspectRouteCode;
    }

    public String getInspectRouteDescription() {
        return inspectRouteDescription;
    }

    public void setInspectRouteDescription(String inspectRouteDescription) {
        this.inspectRouteDescription = inspectRouteDescription;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof FormInfo) {
            String planDate = ((FormInfo) o).planDate;
            if (!TextUtils.isEmpty(planDate)) {
                long second = TimeUtils.INSTANCE.getSecond(planDate);
                long nowSecond = TimeUtils.INSTANCE.getSecond(this.planDate);
                long difference  = nowSecond - second;
                if (difference == 0) {
                    return compareFormCode((FormInfo) o);
                } else {
                    if (difference > 0)
                        return -1;
                    else return 1;
                }
            } else {
                return compareFormCode((FormInfo) o);
            }
        }
        return 0;
    }

    @Nullable
    private int compareFormCode(@NonNull FormInfo o) {
        String formCode = o.formCode;
        if (!TextUtils.isEmpty(formCode) && !TextUtils.isEmpty(this.formCode)) {
            int length = formCode.length();
            int nowLen = this.formCode.length();
            if (length > 3 && nowLen > 3) {
                String orderStr = formCode.substring(length - 3);
                String orderStrNow = this.formCode.substring(nowLen - 3);
                try {
                    int order = Integer.parseInt(orderStr);
                    int orderNow = Integer.parseInt(orderStrNow);
                    return orderNow - order;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        }
        return 0;
    }

    @Override
    public int hashCode() {
        StringBuilder buffer = new StringBuilder();
        if (!TextUtils.isEmpty(weather)) {
            buffer.append(weather.hashCode());
        }
        if (!TextUtils.isEmpty(shift)) {
            buffer.append(shift.hashCode());
        }
        buffer.append(startTime);
        if (!TextUtils.isEmpty(startUser)) {
            buffer.append(startUser.hashCode());
        }
        buffer.append(completeTime);
        if (!TextUtils.isEmpty(completeUser)) {
            buffer.append(completeUser.hashCode());
        }
        if (!TextUtils.isEmpty(fillinRemarks)) {
            buffer.append(fillinRemarks.hashCode());
        }
        if (!TextUtils.isEmpty(status)) {
            buffer.append(status.hashCode());
        }
        return buffer.toString().hashCode();
    }
}
