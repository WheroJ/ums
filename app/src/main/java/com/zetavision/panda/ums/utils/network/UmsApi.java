package com.zetavision.panda.ums.utils.network;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by shopping on 2018/1/24 15:20.
 * https://github.com/wheroj
 */

public interface UmsApi {

    @GET("/ums/control/login.mobile?")
    Observable<ResponseBody> login(@Query("USERNAME") String userName, @Query("PASSWORD") String pass, @Query("USERLOCALE") String userLocale);

    @GET("/ums/control/logout.mobile")
    Observable<ResponseBody> logout(@Query("USERNAME") String userName);

    @GET("/ums/control/queryUtilitySystem.mobile")
    Observable<ResponseBody> queryUtilitySystem();

    @GET("/ums/control/queryActionType.mobile")
    Observable<ResponseBody> queryActionType();

    @GET("/ums/control/queryPlannedForm.mobile?")
    Observable<ResponseBody> queryPlannedForm(@Query("utilitySystemId") int utilitySystemId, @Query("actionType") String actionType);

    @GET("/ums/control/downloadForm.mobile?")
    Observable<ResponseBody> downloadMaintForm(@Query("maintFormId") String maintFormId);

    @GET("/ums/control/downloadForm.mobile?")
    Observable<ResponseBody> downloadInspectForm(@Query("inspectFormId") String inspectFormId);

    @POST("/ums/control/uploadForm.mobile")
    Observable<ResponseBody> uploadForm(@Body RequestBody body);

    @POST("/ums/control/uploadForm.mobile")
    Observable<ResponseBody> uploadForm(@Query("forms") String forms);

    @GET("/ums/control/queryWeather.mobile")
    Observable<ResponseBody> queryWeather();

    @GET("/ums/control/queryShift.mobile")
    Observable<ResponseBody> queryShift();

    @POST("/ums/control/uploadFile.mobile")
    Observable<ResponseBody> uploadFile(@Query("functionType") String functionType
            , @Query("fileCategory") String fileCategory, @Query("formCode") String inspectFormCode, @Body() RequestBody fileBody);

    @POST("/ums/control/uploadFileBatch.mobile")
    Observable<ResponseBody> uploadFileBatch(@Query("functionType") String functionType
            , @Query("fileCategory") String fileCategory, @Query("formCode") String inspectFormCode, @Body() RequestBody fileBody);

    @GET("/ums/control/setUserLocale.mobile")
    Observable<ResponseBody> setUserLocale(@Query("USERLOCALE") String userLocale);

    @GET("/ums/control/downloadFile.mobile")
    Observable<ResponseBody> downloadFile(@Query("fileUrl") String fileUrl
            , @Query("fileName") String fileName
            , @Query("isWithRootPath") String isWithRootPath);
}
