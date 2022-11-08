package com.ethanaa.crudstar.repository;

import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientEntityRepository extends JpaRepository<PatientEntity, UUID>, JpaSpecificationExecutor<PatientEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PatientEntity p WHERE p.id = :id")
    Optional<PatientEntity> findByIdWithLock(UUID id);

    @Query(value = "select e.* from patient_entity e " +
            "where jsonb_to_tsvector('English', e.patient, '[\"String\"]') " +
            "@@ websearch_to_tsquery('English', :query)",
            nativeQuery = true
    )
    Page<PatientEntity> search(@Param("query") String query, Pageable pageable);

    @Query(value = "select e.* from patient_entity e " +
            "where jsonb_to_tsvector('English', e.patient, '[\"String\"]') " +
            "@@ websearch_to_tsquery('English', :query)",
            nativeQuery = true
    )
    List<PatientEntity> search(@Param("query") String query);

    // must expose as native query for sorting on json properties
    @Override
    @Query(value = "select e.* from patient_entity e",
            nativeQuery = true)
    Page<PatientEntity> findAll(Pageable pageable);

}
