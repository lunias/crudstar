package com.ethanaa.crudstar.model.persist.patient;

import com.ethanaa.crudstar.model.persist.patient.patch.PatientPatchEntity;
import com.ethanaa.crudstar.model.persist.UUIDEntity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(indexes = {
  @Index(name = "updated_at_index", columnList = "updated_at DESC")
})
@DynamicUpdate
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class PatientEntity extends UUIDEntity {

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Patient patient;

    @OneToMany(
        mappedBy = "patient",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<PatientPatchEntity> patches = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private PatientFieldConfigEntity patientFieldConfigEntity;

    public PatientEntity() {

    }

    public PatientEntity(Patient patient) {
        this.patient = patient;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<PatientPatchEntity> getPatches() {
        return patches;
    }

    public void addPatch(PatientPatchEntity patientPatchEntity) {

        patches.add(patientPatchEntity);
        patientPatchEntity.setPatient(this);
    }

    public void removePatch(PatientPatchEntity patientPatchEntity) {

        patches.remove(patientPatchEntity);
        patientPatchEntity.setPatient(null);
    }

    public PatientFieldConfigEntity getPatientFieldConfigEntity() {
        return patientFieldConfigEntity;
    }

    public void setPatientFieldConfigEntity(PatientFieldConfigEntity patientFieldConfigEntity) {
        this.patientFieldConfigEntity = patientFieldConfigEntity;
    }
}
