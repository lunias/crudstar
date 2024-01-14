package com.ethanaa.crudstar.repository;

import com.ethanaa.crudstar.model.persist.patient.PatientSnapshotEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface PatientSnapshotEntityRepository extends JpaRepository<PatientSnapshotEntity, UUID> {


    @Query(nativeQuery = true,
            value = "SELECT pse.* " +
                    "FROM patient_snapshot_entity pse " +
                    "WHERE pse.as_of <= :localDateTime " +
                    "ORDER BY pse.as_of DESC")
    Page<PatientSnapshotEntity> findSnapshotsAsOfDateTime(Pageable pageable, LocalDateTime localDateTime);
}
