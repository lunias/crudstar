package com.ethanaa.crudstar.model.persist.patient.patch;

import com.ethanaa.crudstar.model.persist.UUIDEntity;
import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@Table
@TypeDef(name = "json", typeClass = JsonType.class)
public class PatientPatchEntity extends UUIDEntity {

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private String patch;

    @ManyToOne(fetch = FetchType.LAZY)
    private PatientEntity patient;

    public PatientPatchEntity() {

    }

    public PatientPatchEntity(String patch) {
        this.patch = patch;
    }

    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }
}
