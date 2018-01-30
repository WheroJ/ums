package com.zetavision.panda.ums.model;

public class FormInfo {


    // 下载状态
    public enum DOWNLOAD_STATUS {
        WAIT,              // 等待开始下载
        PROGRESS,          // 下载中
        DONE,              // 下载完成
        FAIL               // 下载失败
    }
    private DOWNLOAD_STATUS download_status = DOWNLOAD_STATUS.WAIT; //默认等待下载

    public static final String ACTION_TYPE_M = "M";
    public static final String ACTION_TYPE_P = "P";

    public static final String MAINT_TYPE_TBM= "TBM";
    public static final String MAINT_TYPE_CBM= "CBM";

    private int utilitySystemId;
    private String utilitySystemCode;
    private int formId;
    private String formCode;
    private String status;
    private String planDate;
    private String actionType;

    // 保养表单
    private String equipmentCode;
    private String maintType;
    private String maintPeriodName;
    private String maintPeriodDescription;
    private String maintParamTypeCode;
    private String maintParamTypeDescription;

    // 点检表单
    private String inspectRouteCode;
    private String inspectRouteDescription;

    // 根据表单类型获取line或者eqp
    public String getLineOrEqp(){
        if (actionType.equals(ACTION_TYPE_M)) {
            return equipmentCode;
        } else {
            return inspectRouteCode;
        }
    }

    // 根据类型获取说明
    public String getDesc() {
        if (actionType.equals(ACTION_TYPE_M)) {
            if (maintType.equals(MAINT_TYPE_TBM)) {
                return maintPeriodDescription;
            } else {
                return maintParamTypeDescription;
            }
        } else {
            return inspectRouteDescription;
        }
    }

    public DOWNLOAD_STATUS getDownload_status() {
        return download_status;
    }

    public void setDownload_status(DOWNLOAD_STATUS download_status) {
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

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
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
