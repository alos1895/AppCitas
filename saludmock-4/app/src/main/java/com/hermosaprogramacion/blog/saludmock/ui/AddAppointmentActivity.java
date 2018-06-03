package com.hermosaprogramacion.blog.saludmock.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hermosaprogramacion.blog.saludmock.R;
import com.hermosaprogramacion.blog.saludmock.data.api.SaludMockApi;
import com.hermosaprogramacion.blog.saludmock.data.api.mapping.ApiError;
import com.hermosaprogramacion.blog.saludmock.data.api.mapping.ApiMessageResponse;
import com.hermosaprogramacion.blog.saludmock.data.api.mapping.MedicalCentersRes;
import com.hermosaprogramacion.blog.saludmock.data.api.mapping.PostAppointmentsBody;
import com.hermosaprogramacion.blog.saludmock.data.api.model.MedicalCenter;
import com.hermosaprogramacion.blog.saludmock.data.prefs.SessionPrefs;
import com.hermosaprogramacion.blog.saludmock.ui.adapter.MedicalCenterAdapter;
import com.hermosaprogramacion.blog.saludmock.utils.DateTimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddAppointmentActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = AddAppointmentActivity.class.getSimpleName();

    public static final int REQUEST_PICK_DOCTOR_SCHEDULE = 1;

    public static final String EXTRA_TIME_SHEDULE_PICKED
            = "com.hermosaprogramacion.EXTRA_TIME_SCHEDULE_PICKED";
    public static final String EXTRA_MEDICAL_CENTER_ID
            = "com.hermosaprogramacion.EXTRA_MEDICAL_CENTER_ID";
    public static final String EXTRA_DATE_PICKED = "com.hermosaprogramacion.EXTRA_DATE_PICKED";

    private static final String UI_VALUE_MORNING = "Mañana";
    private static final String API_VALUE_MORNING = "morning";

    private static final String UI_VALUE_AFTERNOON = "Tarde";
    private static final String API_VALUE_AFTERNOON = "afternoon";

    private ProgressBar mProgress;
    private View mErrorView;
    private View mCard1;
    private View mCard2;
    private Spinner mMedicalCenterMenu;
    private MedicalCenterAdapter mMedicalCenterAdapter;
    private EditText mDateField;
    private Spinner mTimeScheduleMenu;
    private View mEmptyDoctorView;
    private View mDoctorContentView;
    private TextView mDoctorName;
    private TextView mDoctorScheduleTime;
    private Button mSearchDoctorButton;

    private Retrofit mRestAdapter;
    private SaludMockApi mSaludMockApi;

    private String mMedicalCenterId;
    private Date mDatePicked;
    private String mTimeSchedule;
    private String mDoctorId;
    private String mTimeSlotPicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        mProgress = (ProgressBar) findViewById(R.id.progress);
        mErrorView = findViewById(R.id.error_container);
        mCard1 = findViewById(R.id.appointment_info_card);
        mCard2 = findViewById(R.id.appointment_doctor_card);

        // Centros médicos
        mMedicalCenterMenu = (Spinner) findViewById(R.id.medical_center_menu);
        mMedicalCenterAdapter = new MedicalCenterAdapter(this,
                new ArrayList<MedicalCenter>(0));
        mMedicalCenterMenu.setAdapter(mMedicalCenterAdapter);
        mMedicalCenterMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                MedicalCenter medicalCenter = (MedicalCenter) adapterView.getItemAtPosition(i);
                mMedicalCenterId = medicalCenter.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Fecha
        mDatePicked = DateTimeUtils.getCurrentDate();
        mDateField = (EditText) findViewById(R.id.date_field);
        mDateField.setText(DateTimeUtils.formatDateForUi(mDatePicked));
        mDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        // Jornada
        mTimeScheduleMenu = (Spinner) findViewById(R.id.time_schedule_menu);
        mTimeScheduleMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mTimeSchedule = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Sección del doctor elegido
        mEmptyDoctorView = findViewById(R.id.empty_doctor);
        mDoctorContentView = findViewById(R.id.doctor_content);
        mDoctorName = (TextView) findViewById(R.id.summary_doctor_name);
        mDoctorScheduleTime = (TextView) findViewById(R.id.summary_doctor_time_schedule);


        mSearchDoctorButton = (Button) findViewById(R.id.search_doctor_button);
        mSearchDoctorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDoctorsSchedulesUi();
            }
        });

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

        loadMedicalCenters();

    }

    private void showDoctorsSchedulesUi() {
        Intent requestIntent = new Intent(AddAppointmentActivity.this
                , DoctorsSchedulesActivity.class);
        requestIntent.putExtra(EXTRA_DATE_PICKED, mDatePicked.getTime());
        requestIntent.putExtra(EXTRA_MEDICAL_CENTER_ID, mMedicalCenterId);
        requestIntent.putExtra(EXTRA_TIME_SHEDULE_PICKED, getTimeSchedule());
        startActivityForResult(requestIntent, REQUEST_PICK_DOCTOR_SCHEDULE);
    }

    private String getTimeSchedule() {
        switch (mTimeSchedule) {
            case UI_VALUE_MORNING:
                return API_VALUE_MORNING;
            case UI_VALUE_AFTERNOON:
                return API_VALUE_AFTERNOON;
            default:
                return "";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_PICK_DOCTOR_SCHEDULE == requestCode &&
                RESULT_OK == resultCode) {
            mDoctorId = data.getStringExtra(DoctorsSchedulesActivity.EXTRA_DOCTOR_ID);
            String doctorName = data.getStringExtra(DoctorsSchedulesActivity.EXTRA_DOCTOR_NAME);
            mTimeSlotPicked = data.getStringExtra(DoctorsSchedulesActivity.EXTRA_TIME_SLOT_PICKED);
            showDoctorScheduleSummary(doctorName, mTimeSlotPicked);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        mDatePicked = DateTimeUtils.createDate(year, month, dayOfMonth);
        mDateField.setText(DateTimeUtils.formatDateForUi(mDatePicked));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_appointment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_save_appointment == item.getItemId()) {
            if (emptyDoctor()) {
                showDoctorError();
            } else {
                saveAppointment();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadMedicalCenters() {
        showLoadingIndicator(true);

        String token = SessionPrefs.get(this).getToken();

        mSaludMockApi.getMedicalCenters(token).enqueue(
                new Callback<MedicalCentersRes>() {
                    @Override
                    public void onResponse(Call<MedicalCentersRes> call,
                                           Response<MedicalCentersRes> response) {
                        if (response.isSuccessful()) {
                            MedicalCentersRes res = response.body();
                            List<MedicalCenter> medicalCenters = res.getResults();

                            if (medicalCenters.size() > 0) {
                                showMedicalCenters(medicalCenters);
                            } else {
                                showMedicalCentersError();
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
                    public void onFailure(Call<MedicalCentersRes> call, Throwable t) {
                        showLoadingIndicator(false);
                        showApiError(t.getMessage());
                    }
                });
    }

    private void showMedicalCentersError() {
        mErrorView.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
        mCard1.setVisibility(View.GONE);
        mCard2.setVisibility(View.GONE);
    }

    private void showMedicalCenters(List<MedicalCenter> medicalCenters) {
        mMedicalCenterAdapter.addAll(medicalCenters);
    }

    private void saveAppointment() {
        showLoadingIndicator(true);

        String token = SessionPrefs.get(this).getToken();

        String datetime = DateTimeUtils.joinDateTime(mDatePicked,
                DateTimeUtils.parseUiTime(mTimeSlotPicked));
        String service = "Medicina General";

        PostAppointmentsBody body =
                new PostAppointmentsBody(datetime, service, mDoctorId);

        mSaludMockApi.createAppointment(token, body).enqueue(
                new Callback<ApiMessageResponse>() {
                    @Override
                    public void onResponse(Call<ApiMessageResponse> call,
                                           Response<ApiMessageResponse> response) {

                        if (response.isSuccessful()) {
                            ApiMessageResponse res = response.body();
                            Log.d(TAG, res.getMessage());
                            showAppointmentsUi();

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
                    public void onFailure(Call<ApiMessageResponse> call, Throwable t) {
                        showLoadingIndicator(false);
                        showApiError(t.getMessage());
                    }
                });

    }

    private void showAppointmentsUi() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void showLoadingIndicator(boolean show) {
        mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        mCard1.setVisibility(show ? View.GONE : View.VISIBLE);
        mCard2.setVisibility(show ? View.GONE : View.VISIBLE);
        mErrorView.setVisibility(View.GONE);
    }

    private void showApiError(String error) {
        Snackbar.make(findViewById(android.R.id.content),
                error, Snackbar.LENGTH_LONG).show();
    }

    private void showDoctorError() {
        Snackbar.make(findViewById(android.R.id.content),
                R.string.error_empty_doctor, Snackbar.LENGTH_LONG).show();
    }

    private void showDoctorScheduleSummary(String doctorName, String doctorTime) {
        mEmptyDoctorView.setVisibility(View.GONE);
        mDoctorContentView.setVisibility(View.VISIBLE);

        mDoctorName.setText(doctorName);
        mDoctorScheduleTime.setText(doctorTime);
    }

    private boolean emptyDoctor() {
        return mDoctorId == null || mDoctorId.isEmpty();
    }
}
