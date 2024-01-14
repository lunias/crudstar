package com.ethanaa.crudstar.model.api.exception;

import java.util.UUID;

public class SnapshotNotFoundException extends RuntimeException {

    public SnapshotNotFoundException(UUID patientId) {
        super("Snapshot with id [" + patientId + "] not found");
    }
}
