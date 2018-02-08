package com.zetavision.panda.ums.model;

import org.litepal.crud.DataSupport;

public class ActionInfo extends DataSupport{
    private String actionType;
    private String description;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
