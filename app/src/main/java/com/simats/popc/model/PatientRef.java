package com.simats.popc.model;

import java.io.Serializable;

public class PatientRef implements Serializable {

    public int pk;     // database primary key
    public String pid; // patient ID shown to doctor

    public PatientRef(int pk, String pid) {
        this.pk = pk;
        this.pid = pid;
    }
}
