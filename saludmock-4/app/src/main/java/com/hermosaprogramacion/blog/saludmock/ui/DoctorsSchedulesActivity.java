package com.hermosaprogramacion.blog.saludmock.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hermosaprogramacion.blog.saludmock.R;
import com.hermosaprogramacion.blog.saludmock.data.api.SaludMockApi;
import com.hermosaprogramacion.blog.saludmock.data.api.mapping.ApiError;
import com.hermosaprogramacion.blog.saludmock.data.api.mapping.DoctorsAvailabilityRes;
import com.hermosaprogramacion.blog.saludmock.data.api.model.Doctor;
import com.hermosaprogramacion.blog.saludmock.data.prefs.SessionPrefs;
import com.hermosaprogramacion.blog.saludmock.ui.adapter.DoctorSchedulesAdapter;
import com.hermosaprogramacion.blog.saludmock.utils.DateTimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DoctorsSchedulesActivity extends AppCompatActivity {

    private static final String TAG = DoctorsSchedulesActivity.class.getSimpleName();

    public static final String EXTRA_DOCTOR_ID = "com.hermosaprogramacion.EXTRA_DOCTOR_ID";
    public static final String EXTRA_DOCTOR_NAME = "com.hermosaprogramacion.EXTRA_DOCTOR_NAME";
    public static final String EXTRA_TIME_SLOT_PICKED = "com.hermosaprogramacion.EXTRA_TIME_SLOT_PICKED";

    private RecyclerView mList;
    private DoctorSchedulesAdapter mListAdapter;
    private ProgressBar mProgress;
    private View mEmptyView;

    private Retrofit mRestAdapter;
    private SaludMockApi mSaludMockApi;

    private Date mDateSchedulePicked;
    private String mMedicalCenterId;
    private String mTimeSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctors_schedules);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        mProgress = (ProgressBar) findViewById(R.id.progress);
        mEmptyView = findViewById(R.id.doctors_schedules_empty);

        mList = (RecyclerView) findViewById(R.id.doctors_schedules_list);
        mListAdapter = new DoctorSchedulesAdapter(this,
                new ArrayList<Doctor>(0),
                new DoctorSchedulesAdapter.OnItemListener() {
                    @Override
                    public void onBookingButtonClicked(Doctor bookedDoctor,
                                                       String timeScheduleSelected) {
                        Intent responseIntent = new Intent();
                        responseIntent.putExtra(EXTRA_DOCTOR_ID, bookedDoctor.getId());
                        responseIntent.putExtra(EXTRA_DOCTOR_NAME, bookedDoctor.getName());
                        responseIntent.putExtra(EXTRA_TIME_SLOT_PICKED, timeScheduleSelected);
                        setResult(Activity.RESULT_OK, responseIntent);
                        finish();
                    }
                });
        mList.setAdapter(mListAdapter);

        Intent intent = getIntent();
        mDateSchedulePicked = new Date(intent.getLongExtra(
                AddAppointmentActivity.EXTRA_DATE_PICKED, -1));
        mMedicalCenterId = intent.getStringExtra(AddAppointmentActivity.EXTRA_MEDICAL_CENTER_ID);
        mTimeSchedule = intent.getStringExtra(AddAppointmentActivity.EXTRA_TIME_SHEDULE_PICKED);

        // Crear adaptador Retrofit
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        mRestAdapter = new Retrofit.Builder()
                .baseUrl(SaludMockApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Crear conexión a la API de SaludMock
        mSaludMockApi = mRestAdapter.create(SaludMockApi.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDoctorsSchedules();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadDoctorsSchedules() {
        showLoadingIndicator(true);

        String token = SessionPrefs.get(this).getToken();
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("date", DateTimeUtils.formatDateForApi(mDateSchedulePicked));
        parameters.put("medical-center", mMedicalCenterId);
        parameters.put("time-schedule", mTimeSchedule);

        mSaludMockApi.getDoctorsSchedules(token, parameters).enqueue(
                new Callback<DoctorsAvailabilityRes>() {
                    @Override
                    public void onResponse(Call<DoctorsAvailabilityRes> call,
                                           Response<DoctorsAvailabilityRes> response) {
                        Log.d(TAG, call.request().toString());
                        if (response.isSuccessful()) {
                            DoctorsAvailabilityRes res = response.body();
                            List<Doctor> doctors = res.getResults();

                            if (doctors.size() > 0) {
                                showDoctors(doctors);
                            } else {
                                showEmptyView();
                            }

                        } else {
                            String error = "Ha ocurrido un error. Contacte al administrador";

                            if (response.errorBody().contentType().subtype().equals("json")) {
                                ApiError apiError = ApiError.fromResponseBody(response.errorBody());
                                error = apiError.getMessage();
                                Log.d(TAG, apiError.getDeveloperMessage());
                            } else {
                                try {
                                    Log.d(TAG, response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            showApiError(error);
                        }

                        showLoadingIndicator(false);
                    }

                    @Override
                    public void onFailure(Call<DoctorsAvailabilityRes> call, Throwable t) {
                        showLoadingIndicator(false);
                        showApiError(t.getMessage());
                    }
                });
    }

    private void showApiError(String error) {
        Snackbar.make(findViewById(android.R.id.content),
                error, Snackbar.LENGTH_LONG).show();
    }

    private void showDoctors(List<Doctor> doctors) {
        mListAdapter.setDoctors(doctors);
        mList.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    private void showEmptyView() {
        mEmptyView.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
        mList.setVisibility(View.GONE);
    }

    private void showLoadingIndicator(boolean show) {
        mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        mList.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
