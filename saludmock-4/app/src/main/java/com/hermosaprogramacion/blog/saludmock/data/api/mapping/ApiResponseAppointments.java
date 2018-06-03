package com.hermosaprogramacion.blog.saludmock.data.api.mapping;

import com.hermosaprogramacion.blog.saludmock.data.api.model.AppointmentDisplayList;

import java.util.List;

/**
 * Entidad de dominio para recibir respuesta de la API en el recurso "appointments"
 */

public class ApiResponseAppointments {
    private List<AppointmentDisplayList> results;

    public ApiResponseAppointments(List<AppointmentDisplayList> results) {
        this.results = results;
    }

    public List<AppointmentDisplayList> getResults() {
        return results;
    }
}
