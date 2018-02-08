package com.zetavision.panda.ums.utils.network;

import com.zetavision.panda.ums.utils.LogPrinter;
import com.zetavision.panda.ums.utils.UserPreferences;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by wheroj on 2018/2/5 14:10.
 *
 * @describe
 */

public class AddCookiesInterceptor implements Interceptor {
    private final UserPreferences userPreferences;
    private String lang;

    public AddCookiesInterceptor(String lang) {
        super();
        this.lang = lang;
        userPreferences = new UserPreferences();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request.Builder builder = chain.request().newBuilder();
        Observable.just(userPreferences.getCookie())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String cookie) throws Exception {
                        if (cookie.contains("lang=ch")){
                            cookie = cookie.replace("lang=ch","lang="+lang);
                        }
                        if (cookie.contains("lang=en")){
                            cookie = cookie.replace("lang=en","lang="+lang);
                        }
                        //添加cookie
                        builder.addHeader("cookie", cookie);
                        LogPrinter.i("http ", cookie);
                    }
                });
        return chain.proceed(builder.build());
    }
}
