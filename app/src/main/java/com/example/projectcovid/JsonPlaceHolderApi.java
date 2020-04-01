package com.example.projectcovid;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface JsonPlaceHolderApi {

    @GET("indonesia")
    Call<List<Post>> getPost();

    @GET("provinsi")
    Call<List<Post_Attributes>> getAttributes();
}
