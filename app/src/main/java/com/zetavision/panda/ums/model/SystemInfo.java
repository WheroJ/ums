package com.zetavision.panda.ums.model;

import org.litepal.crud.DataSupport;

public class SystemInfo extends DataSupport {
    private int utilitySystemId;
    private String utilitySystemCode;
    private String utilitySystemName;

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

    public String getUtilitySystemName() {
        return utilitySystemName;
    }

    public void setUtilitySystemName(String utilitySystemName) {
        this.utilitySystemName = utilitySystemName;
    }
}
