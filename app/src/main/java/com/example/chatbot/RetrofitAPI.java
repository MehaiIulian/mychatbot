package com.example.chatbot;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Url;

public interface RetrofitAPI {


    @GET()
    @Headers("Content-Type: multipart/form-data")
    Call<MsgModel> getMessage(@Url String url);
    //to call our API

}
