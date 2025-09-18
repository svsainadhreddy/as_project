package com.example.myapplicationpopc.model;

import com.google.gson.annotations.SerializedName;

public class PatientResponse {

    private int id; // DB PK
    @SerializedName("patient_id")
    private String patientId;
    private String name;
    private String age;
    private String phone;
    private String weight;
    private String gender;
    private String height;
    private String bmi;
    @SerializedName("photo")
    private String photoUrl;

    // Getters & Setters
    public int getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getName() { return name; }
    public String getAge() { return age; }
    public String getPhone() { return phone; }
    public String getWeight() { return weight; }
    public String getGender() { return gender; }
    public String getHeight() { return height; }
    public String getBmi() { return bmi; }
    public String getPhotoUrl() { return photoUrl; }

    public void setId(int id) { this.id = id; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setName(String name) { this.name = name; }
    public void setAge(String age) { this.age = age; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setWeight(String weight) { this.weight = weight; }
    public void setGender(String gender) { this.gender = gender; }
    public void setHeight(String height) { this.height = height; }
    public void setBmi(String bmi) { this.bmi = bmi; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
