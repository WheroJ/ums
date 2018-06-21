package com.zetavision.panda.ums.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public class MyParameterizedType implements ParameterizedType {
    private final Class raw;
    private final Type[] args;
    public MyParameterizedType(Class raw, Type[] args) {
        this.raw = raw;
        this.args = args != null ? args : new Type[0];
    }
    @Override
    public Type[] getActualTypeArguments() {
        return args;
    }
    @Override
    public Type getRawType() {
        return raw;
    }
    @Override
    public Type getOwnerType() {return null;}
}
