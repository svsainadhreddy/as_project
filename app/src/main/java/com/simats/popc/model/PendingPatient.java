package com.simats.popc.model;

import com.google.gson.annotations.SerializedName;

public class PendingPatient {
    private int pk;
    private String id;
    private String name;
    private String status;
    @SerializedName("photo")
    private String photoUrl;

    public int getPk() { return pk; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
