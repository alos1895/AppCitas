package com.hermosaprogramacion.blog.saludmock.data.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Clase para representar "doctores"
 */

public class Doctor {
    @SerializedName("id")
    private String mId;
    @SerializedName("name")
    private String mName;
    @SerializedName("specialty")
    private String mSpecialty;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("times")
    private List<String> mAvailabilityTimes;

    public Doctor(String id, String name, String specialty,
                  String description, List<String> availabilityTimes) {
        mId = id;
        mName = name;
        mSpecialty = specialty;
        mDescription = description;
        mAvailabilityTimes = availabilityTimes;
    }

    public String getId() {
        return mId;
    }


    public String getName() {
        return mName;
    }


    public String getSpecialty() {
        return mSpecialty;
    }

    public String getDescription() {
        return mDescription;
    }

    public List<String> getAvailabilityTimes() {
        return mAvailabilityTimes;
    }
}
