package com.example.tab.contact

import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*
interface ContactService {
    @Headers("accept: application/json",
            "content-type: application/json")
    @POST("/contact/register")
    fun addContact(@Body params: HashMap<String, JsonObject>): Call<String>
}
