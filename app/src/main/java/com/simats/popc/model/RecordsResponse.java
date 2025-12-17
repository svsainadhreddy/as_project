package com.simats.popc.model;

import com.google.gson.annotations.SerializedName;

public class RecordsResponse {
    private int pk;       // Survey primary key
    private String id;    // Patient custom id
    private String name;
    @SerializedName("photoUrl")
    private String photo; // ✅ new field
    private String risk_status;

    // Getters
    public int getPk() { return pk; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getRisk_status(){return risk_status;}


    public String getPhoto() { return photo; }

    // Setters
    public void setPk(int pk) { this.pk = pk; }
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhoto(String photo) { this.photo = photo; }
    public void setRisk_status(){ this.risk_status=risk_status;}

}
