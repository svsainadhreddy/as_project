package com.example.myapplicationpopc.network;


import com.example.myapplicationpopc.model.DoctorResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

import java.util.Map;

public interface ApiService {
    @POST("accounts/register/")
    Call<ResponseBody> registerDoctor(@Body Map<String, String> body);

    @POST("accounts/login/")
    Call<Map<String, String>> loginDoctor(@Body Map<String, String> body);
    @GET("accounts/profile/")
    Call<DoctorResponse> getDoctorProfile(@Header("Authorization") String token);

    @Multipart
    @PUT("accounts/profile/update/")
    Call<DoctorResponse> updateDoctorProfile(
            @Header("Authorization") String token,
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("phone") RequestBody phone,
            @Part("age") RequestBody age,
            @Part("gender") RequestBody gender,
            @Part("specialization") RequestBody specialization,
            @Part MultipartBody.Part profile_image
    );


}
