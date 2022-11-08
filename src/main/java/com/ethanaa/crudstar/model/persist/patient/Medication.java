package com.ethanaa.crudstar.model.persist.patient;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class Medication {

    private String name;

    private String dosage;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate endDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
