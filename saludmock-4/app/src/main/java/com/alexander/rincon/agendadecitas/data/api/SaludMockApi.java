package com.alexander.rincon.agendadecitas.data.api;

import com.alexander.rincon.agendadecitas.data.api.mapping.DoctorsAvailabilityRes;
import com.alexander.rincon.agendadecitas.data.api.mapping.MedicalCentersRes;
import com.alexander.rincon.agendadecitas.data.api.mapping.PostAppointmentsBody;
import com.alexander.rincon.agendadecitas.data.api.model.Affiliate;
import com.alexander.rincon.agendadecitas.data.api.mapping.ApiMessageResponse;
import com.alexander.rincon.agendadecitas.data.api.mapping.ApiResponseAppointments;
import com.alexander.rincon.agendadecitas.data.api.mapping.LoginBody;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * REST service de SaludMock
 */

public interface SaludMockApi {

    @POST("affiliates/login")
    Call<Affiliate> login(@Body LoginBody loginBody);

    @GET("appointments")
    Call<ApiResponseAppointments> getAppointments(
            @Header("Authorization") String token,
            @QueryMap Map<String, Object> parameters
    );

    @Headers("Content-Type: application/json")
    @PATCH("appointments/{id}")
    Call<ApiMessageResponse> cancelAppointment(@Path("id") int appoitmentId,
                                               @Header("Authorization") String token,
                                               @Body HashMap<String, String> statusMap);

    @GET("medical-centers")
    Call<MedicalCentersRes> getMedicalCenters(@Header("Authorization") String token);

    @GET("doctors/availability")
    Call<DoctorsAvailabilityRes> getDoctorsSchedules(@Header("Authorization") String token,
                                                     @QueryMap Map<String, Object> parameters);

    @Headers("Content-Type: application/json")
    @POST("appointments")
    Call<ApiMessageResponse> createAppointment(@Header("Authorization") String token,
                                               @Body PostAppointmentsBody body);

}
