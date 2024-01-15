package com.ethanaa.crudstar.repository;

import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientEntityRepository extends JpaRepository<PatientEntity, UUID>, JpaSpecificationExecutor<PatientEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PatientEntity p WHERE p.id = :id and p.snapshot is null")
    Optional<PatientEntity> findByIdWithLock(UUID id);

    // must expose as native query for sorting on json properties
    @Override
    @Query(value = "select p.* from patient_entity p",
            nativeQuery = true)
    Page<PatientEntity> findAll(Pageable pageable);

}
