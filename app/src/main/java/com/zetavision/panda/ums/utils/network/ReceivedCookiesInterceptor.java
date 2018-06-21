package com.zetavision.panda.ums.utils.network;

import android.text.TextUtils;

import com.zetavision.panda.ums.utils.LogPrinter;
import com.zetavision.panda.ums.utils.UserPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by wheroj on 2018/2/5 14:18.
 *
 * @describe
 */

public class ReceivedCookiesInterceptor implements Interceptor {
    private final UserPreferences userPreferences;

    public ReceivedCookiesInterceptor() {
        super();
        userPreferences = new UserPreferences();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        if (!originalResponse.headers("set-cookie").isEmpty()) {
            final StringBuffer cookieBuffer = new StringBuffer();
            final ArrayList<String> cookieList = new ArrayList<>();
            String cookie = userPreferences.getCookie();
            if (!TextUtils.isEmpty(cookie)) {
                String[] splits = cookie.split(";");
                for (String split : splits) {
                    if (split.contains("JSESSIONID")) {
                        cookieList.add(split);
                        break;
                    }
                }
            }

            List<String> headers = originalResponse.headers("set-cookie");
            Observable.fromIterable(headers)
                    .map(new Function<String, String>() {
                        @Override
                        public String apply(String s) throws Exception {
                            String[] cookieArray = s.split(";");
                            return cookieArray[0];
                        }
                    })
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String cookie) throws Exception {
                            String jsessionid = "JSESSIONID";
                            if (cookie.contains(jsessionid) && !cookieList.isEmpty()) {
                                cookieList.set(0, cookie);
                            } else {
                                cookieList.add(cookie);
                            }
                        }
                    });

            for (String temp : cookieList) {
                cookieBuffer.append(temp).append(";");
            }
            userPreferences.setCookie(cookieBuffer.toString());
            LogPrinter.i("http", "ReceivedCookiesInterceptor = " + cookieBuffer.toString());
        }

        return originalResponse;
    }
}
