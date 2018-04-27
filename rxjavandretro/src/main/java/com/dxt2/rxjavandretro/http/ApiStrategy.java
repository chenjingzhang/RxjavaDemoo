package com.dxt2.rxjavandretro.http;

/**
 * Created by Administrator on 2018/4/27 0027.
 */

public class ApiStrategy {
    public static String baseUrl = "https://api.douban.com/v2/movie/";

    public static ApiService apiService;
    public static ApiService getApiService(){
        if(apiService == null){
            synchronized (Api.class){
                if(apiService == null){
                    new ApiStrategy();
                }
            }
        }
        return apiService;
    }
    private ApiStrategy(){

    }


}
