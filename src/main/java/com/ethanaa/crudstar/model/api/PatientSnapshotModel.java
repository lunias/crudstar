package com.ethanaa.crudstar.model.api;

import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

public class PatientSnapshotModel extends RepresentationModel<PatientSnapshotModel> {

    private LocalDateTime asOf;

    private LocalDateTime createdAt;

    public LocalDateTime getAsOf() {
        return asOf;
    }

    public void setAsOf(LocalDateTime asOf) {
        this.asOf = asOf;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
