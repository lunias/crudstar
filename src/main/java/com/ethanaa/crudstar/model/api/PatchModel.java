package com.ethanaa.crudstar.model.api;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

public class PatchModel extends RepresentationModel<PatchModel> {

    @JsonRawValue
    private String patch;

    private LocalDateTime createdAt;

    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
