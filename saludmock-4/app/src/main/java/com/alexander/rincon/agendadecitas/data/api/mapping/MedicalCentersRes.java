package com.alexander.rincon.agendadecitas.data.api.mapping;

import com.alexander.rincon.agendadecitas.data.api.model.MedicalCenter;

import java.util.List;

/**
 * Objeto de conversión para "medical-centers"
 */

public class MedicalCentersRes {
    private List<MedicalCenter> results;

    public MedicalCentersRes(List<MedicalCenter> results) {
        this.results = results;
    }

    public List<MedicalCenter> getResults() {
        return results;
    }
}
