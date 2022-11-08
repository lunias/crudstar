package com.ethanaa.crudstar.model.persist.patient;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class FollowUp {

    public enum Type {
        ONE_MONTH, THREE_MONTHS, SIX_MONTHS, ONE_YEAR, TWO_YEARS, OTHER;
    }

    public enum Nature {
        OFFICE_VIST, PHONE_CALL, HOUSE_CALL, VIDEO_CALL, OTHER;
    }

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    private Type type;

    private List<Nature> natures;

    private List<Medication> medications;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<Nature> getNatures() {
        return natures;
    }

    public void setNatures(List<Nature> natures) {
        this.natures = natures;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
    }
}
