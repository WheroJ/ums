package com.zetavision.panda.ums.model;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

/**
 * Created by wheroj on 2018/3/1 11:56.
 *
 * @describe
 */

public class SopMap extends DataSupport {
    @Column(unique = true, defaultValue = "undefine")
    public String flowCode;
    public String actonCode;
    public String sopLocalPath;
    public int useCount = 0;
}
