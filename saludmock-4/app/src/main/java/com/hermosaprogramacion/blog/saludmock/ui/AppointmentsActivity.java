package com.hermosaprogramacion.blog.saludmock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hermosaprogramacion.blog.saludmock.R;
import com.hermosaprogramacion.blog.saludmock.data.api.SaludMockApi;
import com.hermosaprogramacion.blog.saludmock.data.api.mapping.ApiError;
import com.hermosaprogramacion.blog.saludmock.data.api.mapping.ApiMessageResponse;
import com.hermosaprogramacion.blog.saludmock.data.api.mapping.ApiResponseAppointments;
import com.hermosaprogramacion.blog.saludmock.data.api.model.AppointmentDisplayList;
import com.hermosaprogramacion.blog.saludmock.data.prefs.SessionPrefs;
import com.hermosaprogramacion.blog.saludmock.ui.adapter.AppointmentsAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppointmentsActivity extends AppCompatActivity {

    public static final String TAG = AppointmentsActivity.class.getSimpleName();

    private static final int STATUS_FILTER_DEFAULT_VALUE = 0;

    private static final int REQUEST_ADD_APPOINMENT = 1;

    private Retrofit mRestAdapter;
    private SaludMockApi mSaludMockApi;

    private RecyclerView mAppointmentsList;
    private AppointmentsAdapter mAppointmentsAdapter;
    private View mEmptyStateContainer;
    private Spinner mStatusFilterSpinner;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Redirección al Login
        if (!SessionPrefs.get(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_appointments);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Remover título de la action bar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mStatusFilterSpinner = (Spinner) findViewById(R.id.toolbar_spinner);
        mStatusFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Ejecutar filtro de citas médicas
                String status = parent.getItemAtPosition(position).toString();
                loadAppointments(status);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<String> statusFilterAdapter =
                new ArrayAdapter<>(
                        toolbar.getContext(),
                        android.R.layout.simple_spinner_item,
                        AppointmentDisplayList.STATES_VALUES);
        mStatusFilterSpinner.setAdapter(statusFilterAdapter);
        statusFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mAppointmentsList = (RecyclerView) findViewById(R.id.list_appointments);
        mAppointmentsAdapter = new AppointmentsAdapter(this, new ArrayList<AppointmentDisplayList>(0));
        mAppointmentsAdapter.setOnItemClickListener(new AppointmentsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppointmentDisplayList clickedAppointment) {
                // TODO: Codificar acciones de click en items
            }

            @Override
            public void onCancelAppointment(AppointmentDisplayList canceledAppointment) {
                // Cancelar cita
                cancelAppointmnent(canceledAppointment.getId());
            }
        });

        mAppointmentsList.setAdapter(mAppointmentsAdapter);

        mEmptyStateContainer = findViewById(R.id.empty_state_container);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddAppointment();
            }
        });

        SwipeRefreshLayout swipeRefreshLayout =
                (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Pedir al servidor información reciente
                mStatusFilterSpinner.setSelection(STATUS_FILTER_DEFAULT_VALUE);
                loadAppointments(getCurrentState());
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_ADD_APPOINMENT == requestCode
                && RESULT_OK == resultCode) {
            showSuccesfullySavedMessage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments(getCurrentState());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_appointments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private void loadAppointments(String rawStatus) {
        // Mostrar estado de carga
        showLoadingIndicator(true);

        // Obtener token de usuario
        String token = SessionPrefs.get(this).getToken();

        String status;

        // Elegir valor del estado según la opción del spinner
        switch (rawStatus) {
            case "Activas":
                status = "Activa";
                break;
            case "Canceladas":
                status = "Cancelada";
                break;
            case "Cumplidas":
                status = "Cumplida";
                break;
            default:
                status = "Todas";
        }

        // Construir mapa de parámetros
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("status", status);
        parameters.put("display", "list");

        // Realizar petición HTTP
        Call<ApiResponseAppointments> call = mSaludMockApi.getAppointments(token, parameters);
        call.enqueue(new Callback<ApiResponseAppointments>() {
            @Override
            public void onResponse(Call<ApiResponseAppointments> call,
                                   Response<ApiResponseAppointments> response) {
                if (!response.isSuccessful()) {
                    // Procesar error de API
                    String error = "Ha ocurrido un error. Contacte al administrador";
                    if (response.errorBody()
                            .contentType()
                            .subtype()
                            .equals("json")) {
                        ApiError apiError = ApiError.fromResponseBody(response.errorBody());

                        error = apiError.getMessage();
                        Log.d(TAG, apiError.getDeveloperMessage());
                    } else {
                        try {
                            // Reportar causas de error no relacionado con la API
                            Log.d(TAG, response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    showLoadingIndicator(false);
                    showErrorMessage(error);
                    return;
                }

                List<AppointmentDisplayList> serverAppointments = response.body().getResults();

                if (serverAppointments.size() > 0) {
                    // Mostrar lista de citas médicas
                    showAppointments(serverAppointments);
                } else {
                    // Mostrar empty state
                    showNoAppointments();
                }

                showLoadingIndicator(false);
            }

            @Override
            public void onFailure(Call<ApiResponseAppointments> call, Throwable t) {
                showLoadingIndicator(false);
                Log.d(TAG, "Petición rechazada:" + t.getMessage());
                showErrorMessage("Error de comunicación");
            }
        });
    }

    private void cancelAppointmnent(int appointmentId) {
        // TODO: Mostrar estado de carga

        // Obtener token de usuario
        String token = SessionPrefs.get(this).getToken();

        // Preparar cuerpo de la petición
        HashMap<String, String> statusMap = new HashMap<>();
        statusMap.put("status", "Cancelada");

        // Enviar petición
        mSaludMockApi.cancelAppointment(appointmentId, token, statusMap).enqueue(
                new Callback<ApiMessageResponse>() {
                    @Override
                    public void onResponse(Call<ApiMessageResponse> call,
                                           Response<ApiMessageResponse> response) {
                        if (!response.isSuccessful()) {
                            // Procesar error de API
                            String error = "Ha ocurrido un error. Contacte al administrador";
                            if (response.errorBody()
                                    .contentType()
                                    .subtype()
                                    .equals("json")) {
                                ApiError apiError = ApiError.fromResponseBody(response.errorBody());

                                error = apiError.getMessage();
                                Log.d(TAG, apiError.getDeveloperMessage());
                            } else {
                                try {
                                    // Reportar causas de error no relacionado con la API
                                    Log.d(TAG, response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            // TODO: Ocultar estado de carga
                            showErrorMessage(error);
                            return;
                        }

                        // Cancelación Exitosa
                        Log.d(TAG, response.body().getMessage());
                        loadAppointments(getCurrentState());
                        // TODO: Ocultar estado de carga
                    }

                    @Override
                    public void onFailure(Call<ApiMessageResponse> call, Throwable t) {
                        // TODO: Ocultar estado de carga
                        Log.d(TAG, "Petición rechazada:" + t.getMessage());
                        showErrorMessage("Error de comunicación");
                    }
                }
        );
    }

    private String getCurrentState() {
        String status = (String) mStatusFilterSpinner.getSelectedItem();
        return status;
    }

    private void showLoadingIndicator(final boolean show) {
        final SwipeRefreshLayout refreshLayout =
                (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(show);
            }
        });
    }

    private void showErrorMessage(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private void showNoAppointments() {
        mAppointmentsList.setVisibility(View.GONE);
        mEmptyStateContainer.setVisibility(View.VISIBLE);
    }

    private void showAppointments(List<AppointmentDisplayList> serverAppointments) {
        mAppointmentsAdapter.swapItems(serverAppointments);

        mAppointmentsList.setVisibility(View.VISIBLE);
        mEmptyStateContainer.setVisibility(View.GONE);
    }

    private void showAddAppointment() {
        Intent intent = new Intent(this, AddAppointmentActivity.class);
        startActivityForResult(intent, REQUEST_ADD_APPOINMENT);
    }

    private void showSuccesfullySavedMessage() {
        Snackbar.make(mFab, R.string.message_appointment_succesfully_saved,
                Snackbar.LENGTH_LONG).show();
    }

}
