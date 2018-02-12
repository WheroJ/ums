package com.zetavision.panda.ums.model;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

public class FormInfo extends DataSupport{


    /**
     * 下载状态
     */
    public static final int WAIT = 1;
    public static final int PROGRESS = 2;
    public static final int DONE = 3;
    public static final int FAIL = 4;

    private int download_status = WAIT; //默认等待下载

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

    // 保养表单
    private String equipmentCode;
    public String equipmentName;
    private String maintType;
    private String maintPeriodName;
    private String maintPeriodDescription;
    private String maintParamTypeCode;
    private String maintParamTypeDescription;

    // 点检表单
    private String inspectRouteCode;
    private String inspectRouteDescription;
    public String inspectPeriodCode;
    public String inspectPeriodDescription;
    public String inspectPeriodName;

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
}
