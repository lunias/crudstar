package com.ethanaa.crudstar.model.api.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID patientId) {
        super("Patient with id [" + patientId + "] not found");
    }
}
