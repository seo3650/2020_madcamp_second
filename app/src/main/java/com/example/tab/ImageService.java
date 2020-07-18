package com.example.tab;

import java.util.ArrayList;

import kotlin.Unit;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImageService {
    @Multipart
    @POST("/image/upload")
    @Headers({"accept: multipart/form-data"})
    Call<ResponseBody> uploadImage(
            @Header("userId") String userId,
            @Part MultipartBody.Part image);
}
