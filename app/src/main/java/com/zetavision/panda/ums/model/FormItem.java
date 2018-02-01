package com.zetavision.panda.ums.model;

import org.litepal.crud.DataSupport;

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
    public String unit;//",
    public String lowerLimit;//5",
    public String upperLimit;//10",
    public String optionValues;//"
    public String remarks;//"
}
