package com.zetavision.panda.ums.utils.network;

import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.UserPreferences;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by shopping on 2017/12/22 10:02.
 * https://github.com/wheroj
 */

public class Client {

//    http://www.juyuejk.com/uniqueComservice2/base.do?method=uploadPic&type=userphoto
    private static final String BASE_URL = Constant.API_BASE_URL;

    public static <T> T getApi(final Class<T> service) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (Constant.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else logging.setLevel(HttpLoggingInterceptor.Level.NONE);

        UserPreferences preferences = new UserPreferences();
        String lang;
        if (preferences.getLanguage().equals(Locale.CHINESE.getLanguage())) {
            //中文
            lang = "ch";
        } else {
            //英文
            lang = "en";
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        OkHttpClient.Builder builder = okHttpClient.newBuilder();
        okHttpClient = builder.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
////                        try {
//                            return chain.proceed(chain.request());
////                        } catch (ConnectException e) {
////                            e.getMessage();
////                            if (Constant.NET_TYPE != 2) {
////                                Constant.NET_TYPE = 2;
////                                Constant.setBaseUrlByType();
////                                Request request = chain.request();
////                                String apiBaseUrl = Constant.API_BASE_URL;
////                                HttpUrl url = request.url().newBuilder().host(apiBaseUrl).build();
////                                request.newBuilder().url(url);
////                                return chain.proceed(request);
////                            }
////                            return null;
////                        }
//                    }
//                })
                .addInterceptor(new AddCookiesInterceptor(lang))
                .addInterceptor(new ReceivedCookiesInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(service);
    }
}

