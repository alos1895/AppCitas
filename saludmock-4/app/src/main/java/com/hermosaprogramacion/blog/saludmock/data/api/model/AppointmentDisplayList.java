package com.hermosaprogramacion.blog.saludmock.data.api.model;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * POJO para las citas médicas
 */

public class AppointmentDisplayList {

    // estados:
    public static List<String> STATES_VALUES =
            Arrays.asList("Todas", "Activas", "Cumplidas", "Canceladas");

    @SerializedName("id")
    private int mId;
    @SerializedName("date_and_time")
    private Date mDateAndTime;
    @SerializedName("service")
    private String mService;
    @SerializedName("status")
    private String mStatus;
    @SerializedName("doctor")
    private String mDoctor;
    @SerializedName("medical_center")
    private String mMedicalCenter;

    public AppointmentDisplayList(int id, Date dateAndTime, String service,
                                  String status, String doctor, String medicalCenter) {
        mId = id;
        mDateAndTime = dateAndTime;
        mService = service;
        mStatus = status;
        mDoctor = doctor;
        mMedicalCenter = medicalCenter;
    }

    public int getId() {
        return mId;
    }

    public Date getDateAndTime() {
        return mDateAndTime;
    }

    public String getDateAndTimeForList() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd \n HH:mm:ss", Locale.getDefault());
        return sdf.format(mDateAndTime);
    }

    public String getService() {
        return mService;
    }

    public String getStatus() {
        return mStatus;
    }

    public String getDoctor() {
        return mDoctor;
    }

    public String getMedicalCenter() {
        return mMedicalCenter;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public void setDateAndTime(Date mDateAndTime) {
        this.mDateAndTime = mDateAndTime;
    }

    public void setService(String mService) {
        this.mService = mService;
    }

    public void setStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public void setDoctor(String mDoctor) {
        this.mDoctor = mDoctor;
    }

    public void setMedicalCenter(String mMedicalCenter) {
        this.mMedicalCenter = mMedicalCenter;
    }
}
