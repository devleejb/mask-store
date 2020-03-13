package com.devleejb.maskstore.client;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StoreService {
    @GET("storesByGeo/json")
    Call<ResponseModel<List<Store>>> getStores(@Query("lat") double lat, @Query("lng") double lng, @Query("m") int m);
}
