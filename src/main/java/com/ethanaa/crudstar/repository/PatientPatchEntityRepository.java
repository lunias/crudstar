package com.ethanaa.crudstar.repository;

import com.ethanaa.crudstar.model.persist.patient.patch.PatientPatchEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PatientPatchEntityRepository extends JpaRepository<PatientPatchEntity, UUID> {

    @Query(nativeQuery = true,
            value = "SELECT ppe.* " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND ppe.snapshot_id IS NULL " +
                    "ORDER BY ppe.created_at DESC")
    Page<PatientPatchEntity> findPatches(Pageable pageable, UUID patientId);

    @Query(nativeQuery = true,
            value = "SELECT ppe.* " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND (ppe.snapshot_id IS NULL " +
                    "    OR ppe.snapshot_id = :snapshotId) " +
                    "ORDER BY ppe.created_at DESC")
    Page<PatientPatchEntity> findSnapshotPatches(Pageable pageable, UUID patientId, UUID snapshotId);

    @Query(nativeQuery = true,
            value = "WITH patients_with_patches AS ( " +
                    "  SELECT patient_entity_id FROM ( " +
                    "    SELECT " +
                    "      DISTINCT ON (ppe.patient_id) ppe.patient_id AS patient_entity_id, ppe.created_at " +
                    "    FROM " +
                    "      patient_patch_entity ppe " +
                    "    WHERE " +
                    "      ppe.created_at <= :localDateTime " +
                    "        AND ppe.snapshot_id IS NULL " +
                    "    ORDER BY " +
                    "      ppe.patient_id, " +
                    "      ppe.created_at DESC " +
                    "  ) distinct_patients " +
                    "  ORDER BY distinct_patients.created_at DESC " +
                    "  LIMIT :pageSize OFFSET :pageNumber * :pageSize " +
                    ") " +
                    "SELECT " +
                    "  patient_entity_id, " +
                    "  ppe.* " +
                    "FROM " +
                    "  patients_with_patches " +
                    "  JOIN patient_patch_entity ppe ON patient_entity_id = ppe.patient_id " +
                    "WHERE " +
                    "  ppe.created_at <= :localDateTime " +
                    "    AND ppe.snapshot_id IS NULL " +
                    "ORDER BY " +
                    "  ppe.created_at ASC")
    List<PatientPatchEntity> findPatchesAsOfDateTime(int pageSize, int pageNumber, LocalDateTime localDateTime);

    @Query(nativeQuery = true,
            value = "WITH patients_with_patches AS ( " +
                    "  SELECT patient_entity_id FROM ( " +
                    "    SELECT " +
                    "      DISTINCT ON (ppe.patient_id) ppe.patient_id AS patient_entity_id, ppe.created_at " +
                    "    FROM " +
                    "      patient_patch_entity ppe " +
                    "    WHERE " +
                    "      ppe.created_at <= :localDateTime " +
                    "        AND (ppe.snapshot_id IS NULL " +
                    "      OR ppe.snapshot_id = :snapshotId)" +
                    "    ORDER BY " +
                    "      ppe.patient_id, " +
                    "      ppe.created_at DESC " +
                    "  ) distinct_patients " +
                    "  ORDER BY distinct_patients.created_at DESC " +
                    "  LIMIT :pageSize OFFSET :pageNumber * :pageSize" +
                    ") " +
                    "SELECT " +
                    "  patient_entity_id, " +
                    "  ppe.* " +
                    "FROM " +
                    "  patients_with_patches " +
                    "  JOIN patient_patch_entity ppe ON patient_entity_id = ppe.patient_id " +
                    "WHERE " +
                    "  ppe.created_at <= :localDateTime " +
                    "    AND (ppe.snapshot_id IS NULL " +
                    "  OR ppe.snapshot_id = :snapshotId) " +
                    "ORDER BY " +
                    "  ppe.created_at ASC")
    List<PatientPatchEntity> findPatchesAsOfDateTime(int pageSize, int pageNumber,
                                                     LocalDateTime localDateTime, UUID snapshotId);

    @Query(nativeQuery = true,
            value = "SELECT ppe.* " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND ppe.snapshot_id = :snapshotId " +
                    "ORDER BY ppe.created_at ASC")
    List<PatientPatchEntity> findSnapshotPatches(UUID patientId, UUID snapshotId);

    @Query(nativeQuery = true,
            value = "SELECT ppe.* " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.snapshot_id = :snapshotId " +
                    "  AND ppe.patient_id in (:patientIds) " +
                    "GROUP BY ppe.id, ppe.patient_id " +
                    "ORDER BY ppe.patient_id, ppe.created_at ASC")
    List<PatientPatchEntity> findSnapshotPatches(Collection<UUID> patientIds, UUID snapshotId);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT ppe.patient_id) " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.created_at <= :localDateTime " +
                    "  AND ppe.snapshot_id IS NULL")
    Long countPatientsWithPatchesAsOfDateTime(LocalDateTime localDateTime);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT ppe.patient_id) " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.created_at <= :localDateTime " +
                    "  AND (ppe.snapshot_id IS NULL " +
                    "  OR ppe.snapshot_id = :snapshotId)")
    Long countSnapshotPatientsWithPatchesAsOfDateTime(LocalDateTime localDateTime, UUID snapshotId);

    @Query(nativeQuery = true,
            value = "SELECT ppe.* " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND ppe.created_at <= :localDateTime " +
                    "  AND ppe.snapshot_id IS NULL " +
                    "ORDER BY ppe.created_at ASC")
    List<PatientPatchEntity> findPatchesAsOfDateTime(UUID patientId, LocalDateTime localDateTime);

    @Query(nativeQuery = true,
            value = "SELECT ppe.* " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND ppe.created_at <= :localDateTime " +
                    "  AND ppe.snapshot_id IS NULL " +
                    "ORDER BY ppe.created_at DESC")
    Page<PatientPatchEntity> findPatchesAsOfDateTime(Pageable pageable, UUID patientId, LocalDateTime localDateTime);

    @Query(nativeQuery = true,
            value = "SELECT ppe.* " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND ppe.created_at <= :localDateTime " +
                    "    AND (ppe.snapshot_id IS NULL " +
                    "  OR ppe.snapshot_id = :snapshotId)" +
                    "ORDER BY ppe.created_at ASC")
    List<PatientPatchEntity> findPatchesAsOfDateTime(UUID patientId, LocalDateTime localDateTime, UUID snapshotId);

    @Query(nativeQuery = true,
            value = "SELECT ppe.* " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND ppe.created_at <= :localDateTime " +
                    "    AND (ppe.snapshot_id IS NULL " +
                    "  OR ppe.snapshot_id = :snapshotId) " +
                    "ORDER BY ppe.created_at DESC")
    Page<PatientPatchEntity> findPatchesAsOfDateTime(
            Pageable pageable, UUID patientId, LocalDateTime localDateTime, UUID snapshotId);

    @Query(nativeQuery = true,
            value = "SELECT ppe.* " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND ppe.snapshot_id IS NULL " +
                    "ORDER BY ppe.created_at ASC " +
                    "LIMIT :numPatches")
    List<PatientPatchEntity> findFirstPatches(UUID patientId, long numPatches);

    @Query(nativeQuery = true,
            value = "SELECT ppe.* " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND (ppe.created_at <= :localDateTime " +
                    "    AND ppe.snapshot_id IS NULL) " +
                    "  OR ppe.snapshot_id = :snapshotId " +
                    "ORDER BY ppe.created_at ASC " +
                    "LIMIT :numPatches")
    List<PatientPatchEntity> findFirstSnapshotPatches(
            UUID patientId, UUID snapshotId, LocalDateTime localDateTime, long numPatches);

    public static interface PatientIdPatchTuple {
        String getPatientId();
        String getPatch();

        LocalDateTime getCreatedAt();
    }

    @Query(nativeQuery = true,
            value = "SELECT Cast(ppe.patient_id as varchar) as patientId, " +
                    "    Cast(ppe.patch as text) as patch, " +
                    "    ppe.created_at as createdAt " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id IN (:patientIds) " +
                    "  AND ppe.snapshot_id IS NULL " +
                    "GROUP BY ppe.id, ppe.patient_id " +
                    "ORDER BY ppe.patient_id, ppe.created_at ASC")
    List<PatientIdPatchTuple> findPatches(List<UUID> patientIds);

    @Query(nativeQuery = true,
            value = "SELECT Cast(ppe.patient_id as varchar) as patientId, " +
                    "    Cast(ppe.patch as text) as patch, " +
                    "    ppe.created_at as createdAt " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id IN (:patientIds) " +
                    "  AND ppe.created_at <= :localDateTime " +
                    "  AND (ppe.snapshot_id IS NULL " +
                    "    OR ppe.snapshot_id = :snapshotId) " +
                    "GROUP BY ppe.id, ppe.patient_id " +
                    "ORDER BY ppe.patient_id, ppe.created_at ASC")
    List<PatientIdPatchTuple> findPatches(List<UUID> patientIds, UUID snapshotId, LocalDateTime localDateTime);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(ppe.id) " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND ppe.snapshot_id IS NULL")
    Long countPatches(UUID patientId);

    public static interface PatchCount {
        String getPatientId();
        Long getCount();
    }

    @Query(nativeQuery = true,
            value = "SELECT Cast(ppe.patient_id as varchar) AS patientId, COUNT(ppe.id) AS count " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id IN (:patientIds) " +
                    "  AND ppe.snapshot_id IS NULL " +
                    "GROUP BY ppe.patient_id")
    List<PatchCount> countPatches(List<UUID> patientIds);

    @Query(nativeQuery = true,
            value = "SELECT Cast(ppe.patient_id as varchar) AS patientId, COUNT(ppe.id) AS count " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id IN (:patientIds) " +
                    "  AND (ppe.snapshot_id IS NULL " +
                    "    OR ppe.snapshot_id = :snapshotId)" +
                    "GROUP BY ppe.patient_id")
    List<PatchCount> countPatches(List<UUID> patientIds, UUID snapshotId);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(ppe.id) " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND ppe.created_at <= :localDateTime " +
                    "  AND ppe.snapshot_id IS NULL")
    long countPatchesAsOfDateTime(UUID patientId, LocalDateTime localDateTime);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(ppe.id) " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id = :patientId " +
                    "  AND ( " +
                    "    (ppe.created_at <= :localDateTime AND ppe.snapshot_id IS NULL) " +
                    "    OR ppe.snapshot_id = :snapshotId " +
                    "  )")
    long countSnapshotPatches(UUID patientId, LocalDateTime localDateTime, UUID snapshotId);

    @Query(nativeQuery = true,
            value = "SELECT Cast(ppe.patient_id as varchar) AS patientId, COUNT(ppe.id) AS count " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id in (:patientIds) " +
                    "  AND ppe.created_at <= :localDateTime " +
                    "  AND ppe.snapshot_id IS NULL " +
                    "GROUP BY ppe.patient_id")
    List<PatchCount> countPatchesAsOfDateTime(List<UUID> patientIds, LocalDateTime localDateTime);

    @Query(nativeQuery = true,
            value = "SELECT Cast(ppe.patient_id as varchar) AS patientId, COUNT(ppe.id) AS count " +
                    "FROM patient_patch_entity ppe " +
                    "WHERE ppe.patient_id in (:patientIds) " +
                    "  AND ( " +
                    "    (ppe.created_at <= :localDateTime AND ppe.snapshot_id IS NULL) " +
                    "    OR ppe.snapshot_id = :snapshotId " +
                    "  ) " +
                    "GROUP BY ppe.patient_id")
    List<PatchCount> countPatchesAsOfDateTime(List<UUID> patientIds, LocalDateTime localDateTime, UUID snapshotId);
}
