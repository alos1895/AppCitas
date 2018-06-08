package com.alexander.rincon.agendadecitas.ui;

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

import com.alexander.rincon.agendadecitas.R;
import com.alexander.rincon.agendadecitas.data.api.ApiClient;
import com.alexander.rincon.agendadecitas.data.api.AgendaCitasApi;
import com.alexander.rincon.agendadecitas.data.api.mapping.ApiError;
import com.alexander.rincon.agendadecitas.data.api.mapping.DoctorsAvailabilityRes;
import com.alexander.rincon.agendadecitas.data.api.model.Doctor;
import com.alexander.rincon.agendadecitas.data.prefs.SessionPrefs;
import com.alexander.rincon.agendadecitas.ui.adapter.DoctorSchedulesAdapter;
import com.alexander.rincon.agendadecitas.utils.DateTimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorsSchedulesActivity extends AppCompatActivity {

    private static final String TAG = DoctorsSchedulesActivity.class.getSimpleName();

    public static final String EXTRA_DOCTOR_ID = "com.hermosaprogramacion.EXTRA_DOCTOR_ID";
    public static final String EXTRA_DOCTOR_NAME = "com.hermosaprogramacion.EXTRA_DOCTOR_NAME";
    public static final String EXTRA_TIME_SLOT_PICKED = "com.hermosaprogramacion.EXTRA_TIME_SLOT_PICKED";
    public static final String EXTRA_DOCTOR_SPECIALTY = "com.hermosaprogramacion.EXTRA_DOCTOR_SPECIALTY";

    private RecyclerView mList;
    private DoctorSchedulesAdapter mListAdapter;
    private ProgressBar mProgress;
    private View mEmptyView;

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
                        responseIntent.putExtra(EXTRA_DOCTOR_SPECIALTY, bookedDoctor.getSpecialty());
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

        AgendaCitasApi agendaCitasApi = ApiClient.getClient().create(AgendaCitasApi.class);
        Call<DoctorsAvailabilityRes> call = agendaCitasApi.getDoctorsSchedules(token, parameters);
        call.enqueue(new Callback<DoctorsAvailabilityRes>() {
            @Override
            public void onResponse(Call<DoctorsAvailabilityRes> call, Response<DoctorsAvailabilityRes> response) {
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
