package com.example.myapplicationpopc.model;

public class DashboardResponse {
    private int total_surveyed;
    private int pending_surveys;
    private int high_risk_patients;

    public int getTotal_surveyed() { return total_surveyed; }
    public int getPending_surveys() { return pending_surveys; }
    public int getHigh_risk_patients() { return high_risk_patients; }
}
