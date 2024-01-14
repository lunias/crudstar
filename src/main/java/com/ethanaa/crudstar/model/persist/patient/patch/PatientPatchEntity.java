package com.ethanaa.crudstar.model.persist.patient.patch;

import com.ethanaa.crudstar.model.persist.UUIDEntity;
import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import com.ethanaa.crudstar.model.persist.patient.PatientSnapshotEntity;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@Table(indexes = {
        @Index(name = "created_at_index", columnList = "created_at ASC")
})
@TypeDef(name = "json", typeClass = JsonType.class)
public class PatientPatchEntity extends UUIDEntity {

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private String patch;

    @ManyToOne(fetch = FetchType.LAZY)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    private PatientSnapshotEntity snapshot;

    public PatientPatchEntity() {

    }

    public PatientPatchEntity(String patch) {
        this.patch = patch;
    }

    public PatientPatchEntity(String patch, PatientEntity patient, PatientSnapshotEntity snapshot) {
        this(patch, snapshot);
        this.patient = patient;
    }

    public PatientPatchEntity(String patch, PatientSnapshotEntity snapshot) {
        this(patch);
        this.snapshot = snapshot;
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

    public PatientSnapshotEntity getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(PatientSnapshotEntity snapshot) {
        this.snapshot = snapshot;
    }
}
