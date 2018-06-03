package com.hermosaprogramacion.blog.saludmock.data.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Objeto plano para representar centros médicos
 */

public class MedicalCenter {
    @SerializedName("id")
    private String mId;
    @SerializedName("name")
    private String mName;
    @SerializedName("address")
    private String mAddress;

    public MedicalCenter(String id, String name, String address) {
        mId = id;
        mName = name;
        mAddress = address;
    }

    public String getId() {
        return mId;
    }

    public void seId(String mId) {
        this.mId = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }
}
