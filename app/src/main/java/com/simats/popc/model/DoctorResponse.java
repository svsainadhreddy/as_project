package com.simats.popc.model;

import com.google.gson.annotations.SerializedName;

public class DoctorResponse {
    @SerializedName("doctor_id")
    private String doctorId;

    private String username;
    private String email;
    private String phone;
    private String age;
    private String gender;
    private String name;

    private String specialization;

    @SerializedName("profile_image_url")  //  not "profile_image"
    private String profileImageUrl;


    // ---- Getters ----
    public String getDoctorId() { return doctorId; }
    public String getUsername() { return username; }
    public String getName() { return name; }


    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAge() { return age; }
    public String getGender() { return gender; }
    public String getSpecialization() { return specialization; }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // ---- Setters ----
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAge(String age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

}
