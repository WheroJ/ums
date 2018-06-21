package com.zetavision.panda.ums.model;

import org.litepal.crud.DataSupport;

/**
 * Created by wheroj on 2018/2/4 17:53.
 *
 * @describe
 */

public class Shift extends DataSupport{
    /**
     * 班次
     */
    public String shift;

    /**
     * 描述
     */
    public String description;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Shift) {
            return ((Shift) obj).shift.equals(shift);
        }
        return super.equals(obj);
    }
}
