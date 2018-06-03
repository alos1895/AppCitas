package com.hermosaprogramacion.blog.saludmock.data.api.mapping;

import com.hermosaprogramacion.blog.saludmock.data.api.model.Doctor;

import java.util.List;

/**
 * Objeto de conversión para doctors/availability
 */

public class DoctorsAvailabilityRes {
    private List<Doctor> results;

    public DoctorsAvailabilityRes(List<Doctor> results) {
        this.results = results;
    }

    public List<Doctor> getResults() {
        return results;
    }
}
