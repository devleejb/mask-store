package com.devleejb.maskstore.client;

import com.squareup.moshi.Moshi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class StoreClient {
    private static StoreClient INSTANCE = null;
    private static StoreService storeService = null;

    public static StoreClient getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new StoreClient();
        }

        return INSTANCE;
    }

    private StoreClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Moshi moshi = new Moshi.Builder().build();

        storeService = new Retrofit.Builder()
                .baseUrl("https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(client)
                .build()
                .create(StoreService.class);
    }

    public static StoreService getStoreService() {
        return storeService;
    }
}
