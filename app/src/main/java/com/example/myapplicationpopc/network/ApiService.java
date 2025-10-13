package com.example.myapplicationpopc.network;

import androidx.annotation.Nullable;

import com.example.myapplicationpopc.model.DashboardResponse;
import com.example.myapplicationpopc.model.Dashboardgraph;
import com.example.myapplicationpopc.model.DoctorResponse;
import com.example.myapplicationpopc.model.PatientResponse;
import com.example.myapplicationpopc.model.PendingPatient;
import com.example.myapplicationpopc.model.RecordsResponse;
import com.example.myapplicationpopc.model.SurveyDisplayResponse;
import com.example.myapplicationpopc.model.SurveyRequest;
import com.example.myapplicationpopc.model.SurveyResponse;
import com.example.myapplicationpopc.model.SurveySectionRisk;
import com.google.gson.JsonObject;

import org.json.JSONObject;

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
            @Nullable @Part("patient_id") RequestBody patientId,
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


    // POST create survey
// POST create survey
    @POST("api/surveys/")
    Call<SurveyResponse> createSurvey(
            @Header("Authorization") String token,
            @Body SurveyRequest surveyRequest
    );

    // GET survey by patient
    @GET("api/surveys/patient/{patient_id}/")
    Call<SurveyDisplayResponse> getSurveyByPatient(
            @Header("Authorization") String token,   // ✅ add token
            @Path("patient_id") int patientId
    );

    @GET("api/dashboard/")
    Call<DashboardResponse> getDashboard(@Header("Authorization") String token);


    @GET("api/surveys/patient-risk/{patient_id}/")
    Call<List<SurveySectionRisk>> getPatientSurveyRisk(
            @Header("Authorization") String token,
            @Path("patient_id") int patientId
    );

    @GET("api/surveys/not-completed/")
    Call<List<PendingPatient>> getPendingSurveys(@Header("Authorization") String token);

        // Add this method
        // ✅ Return RecordsResponse which includes pk, patient_id, and name
        // Make sure this matches exactly the method you're calling in SurveyListActivity
     @GET("api/surveys/completed/")
     Call<List<RecordsResponse>> listCompletedPatients(@Header("Authorization") String token);

    @GET("api/surveys/stats/")
    Call<Dashboardgraph> getDashboardStats(
            @Header("Authorization") String token
    );

    @GET("accounts/profile")
    Call<DoctorResponse> getDoctorimg(@Header("Authorization") String token);


    @PUT("accounts/change-username/")
    Call<Void> changeUsername(@Header("Authorization") String token, @Body Map<String, String> body);

    @PUT("accounts/change-password/")
    Call<Void> changePassword(@Header("Authorization") String token, @Body Map<String, String> body);

}
