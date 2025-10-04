package com.example.myapplicationpopc.model;

public class RecordsResponse {
    private int pk;       // Survey primary key
    private String id;    // Patient custom id
    private String name;

    // Getters
    public int getPk() { return pk; }
    public String getId() { return id; }
    public String getName() { return name; }

    // Setters
    public void setPk(int pk) { this.pk = pk; }
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
}
