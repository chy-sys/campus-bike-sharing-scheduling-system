package com.example.bikeuserapp.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 统一的接口访问入口：集中管理后端地址，并自动为请求附加 JWT
 */
public class ApiClient {

    /** 改成你电脑的局域网IP，不能用 localhost */
    private static final String BASE_URL = "http://192.168.1.37:8001";

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request original = chain.request();
                String token = SpUtil.getString(SpUtil.KEY_TOKEN, "");
                if (token == null || token.isEmpty()) {
                    return chain.proceed(original);
                }
                Request authorized = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(authorized);
            })
            .build();

    public static OkHttpClient client() {
        return CLIENT;
    }

    public static String url(String path) {
        return BASE_URL + path;
    }
}
