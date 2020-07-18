package com.example.tab;

import com.google.gson.JsonObject;

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AccountService {
    @POST("/account/register")
    Call<Unit> addAccount(@Header("userId") String userId);
}
