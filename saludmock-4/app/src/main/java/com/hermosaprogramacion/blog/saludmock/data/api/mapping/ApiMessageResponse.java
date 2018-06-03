package com.hermosaprogramacion.blog.saludmock.data.api.mapping;

import com.google.gson.annotations.SerializedName;

/**
 * POJO para respuestas de la API de tipo mensaje
 */

public class ApiMessageResponse {
    
    @SerializedName("status")
    private int mStatus;
    @SerializedName("message")
    private String mMessage;

    public ApiMessageResponse(int status, String message) {
        mStatus = status;
        mMessage = message;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getMessage() {
        return mMessage;
    }
}
