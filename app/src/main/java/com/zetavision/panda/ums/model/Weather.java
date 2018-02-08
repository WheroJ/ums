package com.zetavision.panda.ums.model;

import org.litepal.crud.DataSupport;

/**
 * Created by wheroj on 2018/2/4 17:54.
 *
 * @describe
 */

public class Weather extends DataSupport{
    public String weather;
    public String description;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Weather) {
            return ((Weather) obj).weather.equals(weather);
        }
        return super.equals(obj);
    }
}
