package com.ethanaa.crudstar.repository;

import com.ethanaa.crudstar.model.persist.patient.patch.PatientPatchEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatientPatchEntityRepository extends JpaRepository<PatientPatchEntity, UUID> {

    Page<PatientPatchEntity> findByPatientIdOrderByCreatedAtDesc(Pageable pageable, UUID patientId);
}
