package com.zetavision.panda.ums.model;

import org.litepal.crud.DataSupport;

public class User extends DataSupport{
    public String USERNAME;
    public String PASS;
    public long loginTime = -1;
    public int expireIn = 8*60*60;
    public static final int LOGIN = 1;
    public static final int LOGINOUT = 2;
    public int isCurrentLogin = LOGINOUT;
}
