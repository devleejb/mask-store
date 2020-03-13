package com.devleejb.maskstore.client;

import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import retrofit2.Call;

public class ClientHelper {
    public static List<Store> getStoresByLoc(double lat, double lng, int m) {
        final Call<ResponseModel<List<Store>>> responseModelCall = StoreClient
                .getINSTANCE()
                .getStoreService()
                .getStores(lat, lng, m);

        FutureTask<List<Store>> futureTask = new FutureTask<>(new Callable<List<Store>>() {
            @Override
            public List<Store> call() throws Exception {
                ResponseModel<List<Store>> responseModel = responseModelCall.execute().body();


                Log.d("Count", "" + responseModel.getCount());

                return responseModel.getStores();
            }
        });

        new Thread(futureTask).start();

        try {
            return futureTask.get();
        } catch (Exception e) {
            Log.d("Err", "err");
            return null;
        }
    }
}
