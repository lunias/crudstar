package com.ethanaa.crudstar.model.persist.patient;

import com.ethanaa.crudstar.model.persist.UUIDEntity;
import com.ethanaa.crudstar.model.persist.patient.patch.PatientPatchEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(indexes = {
        @Index(name = "as_of_index", columnList = "as_of ASC")
})
public class PatientSnapshotEntity extends UUIDEntity {

    @Column(name = "as_of")
    private LocalDateTime asOf;

    @OneToMany(
            mappedBy = "snapshot",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<PatientPatchEntity> patches = new ArrayList<>();

    public PatientSnapshotEntity() {

    }

    public PatientSnapshotEntity(LocalDateTime asOf) {
        this.asOf = asOf;
    }

    public List<PatientPatchEntity> getPatches() {
        return patches;
    }

    public LocalDateTime getAsOf() {
        return asOf;
    }

    public void setAsOf(LocalDateTime asOf) {
        this.asOf = asOf;
    }

    public void addPatch(PatientPatchEntity patientPatchEntity) {

        patches.add(patientPatchEntity);
        patientPatchEntity.setSnapshot(this);
    }

    public void removePatch(PatientPatchEntity patientPatchEntity) {

        patches.remove(patientPatchEntity);
        patientPatchEntity.setSnapshot(null);
    }
}
