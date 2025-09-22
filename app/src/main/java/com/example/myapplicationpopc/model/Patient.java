package com.example.myapplicationpopc.model;


public class Patient {
    private int id;             // Auto-increment primary key in DB
    private String patientId;   // Unique patient ID (entered by doctor)
    private String name;
    private int age;
    private String phone;
    private double weight;
    private String gender;
    private double height;
    private double bmi;
    private int doctorId;       // Foreign key -> doctor who owns this patient

    // --- Constructors ---
    public Patient() {}

    public Patient(int id, String patientId, String name, int age, String phone,
                   double weight, String gender, double height, double bmi, int doctorId) {
        this.id = id;
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.weight = weight;
        this.gender = gender;
        this.height = height;
        this.bmi = bmi;
        this.doctorId = doctorId;
    }

    // --- Getters & Setters ---
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getHeight() {
        return height;
    }
    public void setHeight(double height) {
        this.height = height;
    }

    public double getBmi() {
        return bmi;
    }
    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public int getDoctorId() {
        return doctorId;
    }
    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    // --- Utility method to auto-calc BMI ---
    public void calculateBmi() {
        if (height > 0) {
            this.bmi = weight / Math.pow(height / 100.0, 2); // height in cm → meters
        }
    }

    // --- toString() for debugging ---
    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", patientId='" + patientId + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", phone='" + phone + '\'' +
                ", weight=" + weight +
                ", gender='" + gender + '\'' +
                ", height=" + height +
                ", bmi=" + bmi +
                ", doctorId=" + doctorId +
                '}';
    }
}
