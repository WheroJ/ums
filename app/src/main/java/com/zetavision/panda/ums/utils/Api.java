package com.zetavision.panda.ums.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zetavision.panda.ums.model.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Api {
    private OkHttpClient http;

    public Api(final Context context) {
        this.http = new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)//连接超时(单位:秒)
                .writeTimeout(600, TimeUnit.SECONDS)//写入超时(单位:秒)
                .readTimeout(600, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        Gson gson = new Gson();
                        UserPreferences userPreferences = new UserPreferences();
                        userPreferences.setCookie(url.host(), gson.toJson(cookies));
                    }
                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        Gson gson = new Gson();
                        UserPreferences userPreferences = new UserPreferences();
                        return gson.fromJson(userPreferences.getCookie(url.host()), new TypeToken<List<Cookie>>() {}.getType());
                    }
                }).build();
    }

    public Flowable<Result> post(final String action, final RequestBody body) {
        Request request = new Request.Builder().url(Constant.API_BASE_URL + action).post(body)
//                        .addHeader("X-Requested-With", "XMLHttpRequest")
                .build();
        return CallRequest(request);
    }

    public Flowable<Result> get(final String action) {
        Request request = new Request.Builder().url(Constant.API_BASE_URL + action).get().build();
        return this.CallRequest(request);
    }

    private Flowable<Result> CallRequest(final Request request) {
        return Flowable.create(new FlowableOnSubscribe<Result>() {
            @Override
            public void subscribe(final FlowableEmitter<Result> emitter) throws Exception {
                http.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        emitter.onError(new Throwable(e.getMessage()));
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String resultString = response.body().string();
                            LogPrinter.i("NET_API", resultString);
                            JSONObject resultObejct = new JSONObject(resultString);
                            Result result = new Result();
                            result.setReturnCode(resultObejct.getInt("returnCode"));
                            result.setReturnMessage(resultObejct.getString("returnMessage"));
                            result.setReturnData(resultObejct.getString("returnData"));
                            if (result.getReturnCode() == 0) {
                                emitter.onNext(result);
                                emitter.onComplete();
                            } else {
                                emitter.onError(new Throwable(result.getReturnMessage()));
                            }
                        } catch (JSONException e) {
                            emitter.onError(new Throwable(e.getMessage()));
                        }
                    }
                });
            }
        }, BackpressureStrategy.BUFFER).observeOn(AndroidSchedulers.mainThread());
    }
}
