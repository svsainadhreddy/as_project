package com.example.myapplicationpopc.network;

import com.example.myapplicationpopc.model.DoctorResponse;
import com.example.myapplicationpopc.model.PatientResponse;
import com.example.myapplicationpopc.model.SurveyRequest;
import com.example.myapplicationpopc.model.SurveyResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
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

    @Multipart
    @POST("patients/create/")
    Call<PatientResponse> createPatient(
            @Header("Authorization") String token,
            @Part("patient_id") RequestBody patientId,
            @Part("name") RequestBody name,
            @Part("age") RequestBody age,
            @Part("phone") RequestBody phone,
            @Part("weight") RequestBody weight,
            @Part("gender") RequestBody gender,
            @Part("height") RequestBody height,
            @Part("bmi") RequestBody bmi,
            @Part MultipartBody.Part photo
    );

    @Multipart
    @PUT("patients/{id}/update/")
    Call<PatientResponse> updatePatient(
            @Header("Authorization") String token,
            @Path("id") int patientPk,
            @Part("patient_id") RequestBody patientId,
            @Part("name") RequestBody name,
            @Part("age") RequestBody age,
            @Part("phone") RequestBody phone,
            @Part("weight") RequestBody weight,
            @Part("gender") RequestBody gender,
            @Part("height") RequestBody height,
            @Part("bmi") RequestBody bmi,
            @Part MultipartBody.Part photo
    );

    @GET("patients/{id}/")
    Call<PatientResponse> getPatient(
            @Header("Authorization") String token,
            @Path("id") int patientId
    );

    @GET("patients/")
    Call<List<PatientResponse>> listPatients(
            @Header("Authorization") String token,
            @Query("q") String query // optional search query
    );

    @DELETE("patients/{id}/delete/")
    Call<Void> deletePatient(@Header("Authorization") String token, @Path("id") String patientId);


    // POST /api/surveys/   (adjust endpoint to match your Django backend)
    @Headers("Content-Type: application/json")
    @POST("surveys/")  // your Django url path
    Call<ResponseBody> submitSurvey(@Body SurveyRequest survey);



}
