package com.example.myapplicationpopc.model;

public class PendingPatient {
    private int pk;        // ✅ database primary key
    private String id;        // custom patient_id
    private String name;
    private String status;

    public int getPk() { return pk; }          // use for API POST
    public String getId() { return id; }          // display only
    public String getName() { return name; }
    public String getStatus() { return status; }
}
