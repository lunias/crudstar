package com.ethanaa.crudstar.service;

import com.ethanaa.crudstar.model.api.ApiFilter;
import com.ethanaa.crudstar.model.api.LatestVersion;
import com.ethanaa.crudstar.model.api.Snapshot;
import com.ethanaa.crudstar.model.api.Version;
import com.ethanaa.crudstar.model.api.exception.PatientNotFoundException;
import com.ethanaa.crudstar.model.api.exception.SnapshotNotFoundException;
import com.ethanaa.crudstar.model.persist.UUIDEntity;
import com.ethanaa.crudstar.model.persist.patient.Patient;
import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import com.ethanaa.crudstar.model.persist.patient.PatientSnapshotEntity;
import com.ethanaa.crudstar.model.persist.patient.patch.PatientPatchEntity;
import com.ethanaa.crudstar.repository.PatientEntityRepository;
import com.ethanaa.crudstar.repository.PatientSnapshotEntityRepository;
import com.ethanaa.crudstar.repository.PatientPatchEntityRepository;
import com.ethanaa.crudstar.repository.specification.PatientEntitySpecification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private PatientEntityRepository patientEntityRepository;
    private PatientPatchEntityRepository patientPatchEntityRepository;
    private PatientSnapshotEntityRepository patientSnapshotEntityRepository;

    private ObjectMapper objectMapper;

    @Autowired
    public PatientService(PatientEntityRepository patientEntityRepository,
                          PatientPatchEntityRepository patientPatchEntityRepository,
                          PatientSnapshotEntityRepository patientSnapshotEntityRepository,
                          ObjectMapper objectMapper) {

        this.patientEntityRepository = patientEntityRepository;
        this.patientPatchEntityRepository = patientPatchEntityRepository;
        this.patientSnapshotEntityRepository = patientSnapshotEntityRepository;
        this.objectMapper = objectMapper;
    }

    public LatestVersion<PatientEntity> create(Patient patient) {

        PatientEntity patientEntity = new PatientEntity(patient);
        JsonNode patientJson = objectMapper.valueToTree(patientEntity.getPatient());
        JsonNode createPatch = JsonDiff.asJson(objectMapper.createObjectNode(), patientJson);
        try {
            patientEntity.addPatch(new PatientPatchEntity(objectMapper.writeValueAsString(createPatch)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new LatestVersion<>(patientEntityRepository.save(patientEntity), 1);
    }

    public Snapshot<PatientEntity> create(Patient patient, UUID snapshotId) {

        // TODO create new patient in snapshot, pass snapshot_id to patient?
        return null;
    }

    public void create(Iterable<Patient> patients) {

        List<PatientEntity> patientEntities = new ArrayList<>();
        for (Patient patient : patients) {
            PatientEntity patientEntity = new PatientEntity(patient);
            JsonNode patientJson = objectMapper.valueToTree(patientEntity.getPatient());
            JsonNode createPatch = JsonDiff.asJson(objectMapper.createObjectNode(), patientJson);
            try {
                patientEntity.addPatch(new PatientPatchEntity(objectMapper.writeValueAsString(createPatch)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            patientEntities.add(patientEntity);
        }

        patientEntityRepository.saveAll(patientEntities);
    }

    @Transactional(readOnly = true)
    public Page<Version<PatientEntity>> getAsOfDateTime(Pageable pageable, LocalDateTime dateTime) {

        return getAsOfDateTime(pageable, dateTime, null);
    }

    @Transactional(readOnly = true)
    public Page<Version<PatientEntity>> getAsOfDateTime(Pageable pageable, LocalDateTime dateTime, UUID snapshotId) {

        List<PatientPatchEntity> patches;
        if (snapshotId != null) {
            patches = patientPatchEntityRepository.findPatchesAsOfDateTime(
                    pageable.getPageSize(), pageable.getPageNumber(), dateTime, snapshotId);
        } else {
            patches = patientPatchEntityRepository.findPatchesAsOfDateTime(
                    pageable.getPageSize(), pageable.getPageNumber(), dateTime);
        }
        if (patches.isEmpty()) {
            return new PageImpl<>(new ArrayList<>());
        }

        Map<UUID, Patient> patientMap = new HashMap<>();
        Map<UUID, Long> versionsAsOfDateTime = new HashMap<>();
        long version = 1;
        for (PatientPatchEntity patch : patches) {
            UUID patientId = patch.getPatient().getId();
            Patient patient = patientMap.get(patientId);
            if (patient == null) {
                patient = new Patient();
                version = 1;
            }
            JsonNode patientJson = objectMapper.valueToTree(patient);
            try {
                patientJson = JsonPatch.apply(objectMapper.readTree(patch.getPatch()), patientJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            Patient patchedPatient = objectMapper.convertValue(patientJson, Patient.class);
            patientMap.put(patientId, patchedPatient);
            versionsAsOfDateTime.put(patientId, version);
            version++;
        }

        List<PatientEntity> patientEntities = new ArrayList<>();
        List<UUID> patientEntityIds = new ArrayList<>();
        for (Map.Entry<UUID, Patient> entry : patientMap.entrySet()) {
            PatientEntity patientEntity = new PatientEntity(entry.getValue());
            patientEntity.setId(entry.getKey());
            patientEntities.add(patientEntity);
            patientEntityIds.add(entry.getKey());
        }

        List<Version<PatientEntity>> patientEntityVersions;
        if (snapshotId != null) {
            Map<String, Long> latestVersions = getLatestVersions(patientEntityIds, snapshotId);
            patientEntityVersions = patientEntities.stream().map(
                            patientEntity -> new Version<>(patientEntity,
                                    versionsAsOfDateTime.get(patientEntity.getId()),
                                    latestVersions.get(patientEntity.getId().toString()),
                                    dateTime))
                    .collect(Collectors.toList());

            return new PageImpl<>(patientEntityVersions, pageable,
                    patientPatchEntityRepository.countSnapshotPatientsWithPatchesAsOfDateTime(dateTime, snapshotId));

        } else {
            Map<String, Long> latestVersions = getLatestVersions(patientEntityIds);
            patientEntityVersions = patientEntities.stream().map(
                            patientEntity -> new Version<>(patientEntity,
                                    versionsAsOfDateTime.get(patientEntity.getId()),
                                    latestVersions.get(patientEntity.getId().toString()),
                                    dateTime))
                    .collect(Collectors.toList());

            return new PageImpl<>(patientEntityVersions, pageable,
                    patientPatchEntityRepository.countPatientsWithPatchesAsOfDateTime(dateTime));
        }
    }

    @Transactional(readOnly = true)
    public Version<PatientEntity> getAsOfDateTime(UUID patientId, LocalDateTime dateTime) {

        return getAsOfDateTime(patientId, dateTime, null);
    }

    @Transactional(readOnly = true)
    public Version<PatientEntity> getAsOfDateTime(UUID patientId, LocalDateTime dateTime, UUID snapshotId) {

        List<PatientPatchEntity> patches;
        if (snapshotId != null) {
            patches = patientPatchEntityRepository.findPatchesAsOfDateTime(patientId, dateTime, snapshotId);
        } else {
            patches = patientPatchEntityRepository.findPatchesAsOfDateTime(patientId, dateTime);
        }
        if (patches.isEmpty()) {
            throw new PatientNotFoundException(patientId);
        }

        JsonNode patientJson = objectMapper.createObjectNode();
        for (PatientPatchEntity patch : patches) {
            try {
                patientJson = JsonPatch.apply(objectMapper.readTree(patch.getPatch()), patientJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        Patient patient = objectMapper.convertValue(patientJson, Patient.class);
        PatientEntity patientEntity = new PatientEntity(patient);
        patientEntity.setId(patientId);

        if (snapshotId != null) {
            return new Snapshot<>(patientEntity, patches.size(),
                    getLatestVersion(patientId, snapshotId), dateTime, snapshotId);
        }

        return new Version<>(patientEntity, patches.size(), getLatestVersion(patientId), dateTime);
    }

    public PatientSnapshotEntity createSnapshot(LocalDateTime dateTime) {

        return patientSnapshotEntityRepository.save(new PatientSnapshotEntity(dateTime));
    }

    @Transactional(readOnly = true)
    public Page<PatientSnapshotEntity> getSnapshots(Pageable pageable, LocalDateTime dateTime) {

        return patientSnapshotEntityRepository.findSnapshotsAsOfDateTime(pageable, dateTime);
    }

    @Transactional(readOnly = true)
    public PatientSnapshotEntity getSnapshot(UUID snapshotId) {

        return patientSnapshotEntityRepository.findById(snapshotId)
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));
    }

    @Transactional(readOnly = true)
    public Snapshot<PatientEntity> getSnapshot(UUID patientId, UUID snapshotId) {

        return getSnapshot(patientId, snapshotId, null);
    }

    @Transactional(readOnly = true)
    public Snapshot<PatientEntity> getSnapshot(UUID patientId, UUID snapshotId, LocalDateTime dateTime) {

        PatientSnapshotEntity snapshot = patientSnapshotEntityRepository.findById(snapshotId)
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        LocalDateTime asOf = dateTime;
        if (asOf == null) {
            asOf = snapshot.getAsOf();
        }

        return  (Snapshot<PatientEntity>) getAsOfDateTime(patientId, asOf, snapshotId);
    }

    @Transactional(readOnly = true)
    public Page<Snapshot<PatientEntity>> getSnapshot(Pageable pageable, UUID snapshotId, LocalDateTime dateTime) {

        PatientSnapshotEntity snapshot = patientSnapshotEntityRepository.findById(snapshotId)
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        LocalDateTime asOf = dateTime;
        if (asOf == null) {
            asOf = snapshot.getAsOf();
        }
        Page<Version<PatientEntity>> patientEntityVersions = getAsOfDateTime(pageable, asOf, snapshotId);

        List<Snapshot<PatientEntity>> patientEntitySnapshots = new ArrayList<>();
        for (Version<PatientEntity> patientEntityVersion : patientEntityVersions) {
            patientEntitySnapshots.add(new Snapshot<>(patientEntityVersion, snapshotId));
        }

        return new PageImpl<>(patientEntitySnapshots, pageable, patientEntityVersions.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Snapshot<PatientEntity> getSnapshot(UUID patientId, UUID snapshotId, long version) {

        PatientSnapshotEntity snapshot = patientSnapshotEntityRepository.findById(snapshotId)
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        Version<PatientEntity> patientEntityVersion = getAsOfVersion(patientId, version, snapshotId, snapshot.getAsOf());

        return new Snapshot<>(patientEntityVersion, snapshotId);
    }

    @Transactional(readOnly = true)
    public List<Snapshot<PatientEntity>> getSnapshotVersions(UUID patientId, UUID snapshotId) {

        PatientSnapshotEntity snapshot = patientSnapshotEntityRepository.findById(snapshotId)
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        return getVersions(Collections.singletonList(patientId), snapshotId, LocalDateTime.now())
                .get(patientId.toString())
                .stream()
                .map(patientEntityVersion -> new Snapshot<>(patientEntityVersion, snapshotId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Version<PatientEntity> getAsOfVersion(UUID patientId, long version) {

        return getAsOfVersion(patientId, version, null, null);
    }

    @Transactional(readOnly = true)
    public Version<PatientEntity> getAsOfVersion(UUID patientId, long version, UUID snapshotId, LocalDateTime asOf) {

        long latestVersion;
        if (snapshotId != null) {
            latestVersion = getLatestVersion(patientId, snapshotId);
        } else {
            latestVersion = getLatestVersion(patientId);
        }

        if (version < 1) {
            version = 1;
        }
        if (version > latestVersion) {
            version = latestVersion;
        }

        List<PatientPatchEntity> patches;
        if (snapshotId != null) {
            patches = patientPatchEntityRepository.findFirstSnapshotPatches(patientId, snapshotId, asOf, version);
        } else {
            patches = patientPatchEntityRepository.findFirstPatches(patientId, version);
        }
        if (patches.isEmpty()) {
            throw new PatientNotFoundException(patientId);
        }

        JsonNode patientJson = objectMapper.createObjectNode();
        for (PatientPatchEntity patch : patches) {
            try {
                patientJson = JsonPatch.apply(objectMapper.readTree(patch.getPatch()), patientJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        Patient patient = objectMapper.convertValue(patientJson, Patient.class);
        PatientEntity patientEntity = new PatientEntity(patient);
        patientEntity.setId(patientId);

        LocalDateTime latestPatchCreatedAt = patches.get(patches.size() - 1).getCreatedAt();

        return new Version<>(patientEntity, version, latestVersion, latestPatchCreatedAt);
    }

    @Transactional(readOnly = true)
    public Map<String, List<Version<PatientEntity>>> getVersions(List<UUID> patientIds) {

        return getVersions(patientIds, null, null);
    }

    @Transactional(readOnly = true)
    private Map<String, List<Version<PatientEntity>>> getVersions(List<UUID> patientIds,
                                                                  UUID snapshotId, LocalDateTime dateTime) {

        List<PatientPatchEntityRepository.PatientIdPatchTuple> patchTuples;
        Map<String, Long> latestVersions;
        if (snapshotId != null) {
            patchTuples = patientPatchEntityRepository.findPatches(patientIds, snapshotId, dateTime);
            latestVersions = getLatestVersions(patientIds, snapshotId);
        } else {
            patchTuples = patientPatchEntityRepository.findPatches(patientIds);
            latestVersions = getLatestVersions(patientIds);
        }

        String patientId = null;
        long currentVersion = 1;
        JsonNode patientJson = null;
        Map<String, List<Version<PatientEntity>>> patientEntityVersions = new HashMap<>();

        for (PatientPatchEntityRepository.PatientIdPatchTuple patchTuple : patchTuples) {
            if (!patchTuple.getPatientId().equals(patientId)) {
                patientId = patchTuple.getPatientId();
                currentVersion = 1;
                patientJson = objectMapper.createObjectNode();
            }

            try {
                patientJson = JsonPatch.apply(
                        objectMapper.readTree(patchTuple.getPatch()), patientJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            Patient patient = objectMapper.convertValue(patientJson, Patient.class);
            PatientEntity patientEntity = new PatientEntity(patient);
            patientEntity.setId(UUID.fromString(patientId));

            Version<PatientEntity> patientVersion = new Version<>(patientEntity,
                    currentVersion, latestVersions.get(patientId), patchTuple.getCreatedAt());

            patientEntityVersions.computeIfAbsent(patientId, k -> new ArrayList<>()).add(patientVersion);

            currentVersion++;
        }

        return patientEntityVersions;
    }

    @Transactional(readOnly = true)
    public long getLatestVersion(UUID patientId) {

        return patientPatchEntityRepository.countPatches(patientId);
    }

    @Transactional(readOnly = true)
    public long getLatestVersion(UUID patientId, UUID snapshotId) {

        PatientSnapshotEntity snapshot = patientSnapshotEntityRepository.findById(snapshotId)
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        return patientPatchEntityRepository.countSnapshotPatches(patientId, snapshot.getAsOf(), snapshot.getId());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getLatestVersions(List<UUID> patientIds) {

        List<PatientPatchEntityRepository.PatchCount> latestVersions =
                patientPatchEntityRepository.countPatches(patientIds);

        return latestVersions.stream()
                .collect(Collectors.toMap(
                        PatientPatchEntityRepository.PatchCount::getPatientId,
                        PatientPatchEntityRepository.PatchCount::getCount));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getLatestVersions(List<UUID> patientIds, UUID snapshotId) {

        List<PatientPatchEntityRepository.PatchCount> latestVersions =
                patientPatchEntityRepository.countPatches(patientIds, snapshotId);

        return latestVersions.stream()
                .collect(Collectors.toMap(
                        PatientPatchEntityRepository.PatchCount::getPatientId,
                        PatientPatchEntityRepository.PatchCount::getCount));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getLatestVersionsAsOfDateTime(List<UUID> patientIds,
                                                           LocalDateTime dateTime, UUID snapshotId) {

        List<PatientPatchEntityRepository.PatchCount> latestVersions;
        if (snapshotId != null) {
            latestVersions =
                    patientPatchEntityRepository.countPatchesAsOfDateTime(patientIds, dateTime, snapshotId);
        } else {
            latestVersions =
                    patientPatchEntityRepository.countPatchesAsOfDateTime(patientIds, dateTime);
        }

        return latestVersions.stream()
                .collect(Collectors.toMap(
                        PatientPatchEntityRepository.PatchCount::getPatientId,
                        PatientPatchEntityRepository.PatchCount::getCount));
    }

    @Transactional(readOnly = true)
    public Page<LatestVersion<PatientEntity>> get(Pageable pageable, String query, List<ApiFilter> filters) {

        return search(query, pageable, filters);
    }

    @Transactional(readOnly = true)
    public LatestVersion<PatientEntity> get(UUID patientId) {

        PatientEntity patientEntity = patientEntityRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        return new LatestVersion<>(patientEntity, getLatestVersion(patientId));
    }

    @Transactional(readOnly = true)
    public Page<PatientPatchEntity> getPatches(Pageable pageable, UUID patientId, LocalDateTime asOf) {

        return patientPatchEntityRepository.findPatchesAsOfDateTime(pageable, patientId, asOf);
    }

    @Transactional(readOnly = true)
    public Page<PatientPatchEntity> getPatches(Pageable pageable, UUID patientId) {

        return patientPatchEntityRepository.findPatches(pageable, patientId);
    }

    @Transactional(readOnly = true)
    public Page<PatientPatchEntity> getSnapshotPatches(
            Pageable pageable, UUID patientId, LocalDateTime asOf, UUID snapshotId) {

        return patientPatchEntityRepository.findPatchesAsOfDateTime(pageable, patientId, asOf, snapshotId);
    }

    @Transactional(readOnly = true)
    public Page<PatientPatchEntity> getSnapshotPatches(
            Pageable pageable, UUID patientId, UUID snapshotId) {

        return patientPatchEntityRepository.findSnapshotPatches(pageable, patientId, snapshotId);
    }

    @Transactional(readOnly = true)
    public Page<LatestVersion<PatientEntity>> search(String query, Pageable pageable, List<ApiFilter> filters) {

        PatientEntitySpecification specification = new PatientEntitySpecification(query, filters, pageable);

        // sorting is handled by the specification, create a dummy w/ just page info to send to repository
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<PatientEntity> patientEntities = patientEntityRepository.findAll(specification, pageRequest);
        Map<String, Long> latestVersions = getLatestVersions(
                patientEntities.map(UUIDEntity::getId).getContent());

        return patientEntities.map(patientEntity -> new LatestVersion<>(
                patientEntity, latestVersions.get(patientEntity.getId().toString())));
    }

    public LatestVersion<PatientEntity> update(UUID patientId, Patient patient) {

        PatientEntity patientEntity = patientEntityRepository.findByIdWithLock(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        JsonNode existingPatientJson = objectMapper.valueToTree(patientEntity.getPatient());
        JsonNode updatedPatientJson = objectMapper.valueToTree(patient);
        JsonNode updatePatch = JsonDiff.asJson(existingPatientJson, updatedPatientJson);

        if (updatePatch.isEmpty()) {
            return new LatestVersion<>(patientEntity, getLatestVersion(patientId));
        }

        JsonNode patchedPatientJson = JsonPatch.apply(updatePatch, existingPatientJson);
        Patient patchedPatient = objectMapper.convertValue(patchedPatientJson, Patient.class);

        patientEntity.setPatient(patchedPatient);
        try {
            patientEntity.addPatch(new PatientPatchEntity(objectMapper.writeValueAsString(updatePatch)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new LatestVersion<>(patientEntityRepository.save(patientEntity), getLatestVersion(patientId));
    }

    public Snapshot<PatientEntity> update(UUID patientId, Patient patient, UUID snapshotId) {

        PatientSnapshotEntity snapshot = patientSnapshotEntityRepository.findById(snapshotId)
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        Snapshot<PatientEntity> patientSnapshot = getSnapshot(patientId, snapshotId);

        JsonNode existingPatientJson = objectMapper.valueToTree(patientSnapshot.getEntity().getPatient());
        JsonNode updatedPatientJson = objectMapper.valueToTree(patient);
        JsonNode updatePatch = JsonDiff.asJson(existingPatientJson, updatedPatientJson);

        if (updatePatch.isEmpty()) {
            return patientSnapshot;
        }

        try {
            patientPatchEntityRepository.save(new PatientPatchEntity(
                    objectMapper.writeValueAsString(updatePatch),
                    patientSnapshot.getEntity(),
                    snapshot));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        JsonNode patchedPatientJson = JsonPatch.apply(updatePatch, existingPatientJson);
        Patient patchedPatient = objectMapper.convertValue(patchedPatientJson, Patient.class);

        patientSnapshot.getEntity().setPatient(patchedPatient);

        return new Snapshot<>(patientSnapshot.getEntity(),
                patientSnapshot.getLatestVersion() + 1,
                patientSnapshot.getLatestVersion() + 1,
                snapshotId);
    }

    public LatestVersion<PatientEntity> patch(UUID patientId, JsonNode patientPatch) {

        PatientEntity patientEntity = patientEntityRepository.findByIdWithLock(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        JsonNode patientJson = objectMapper.valueToTree(patientEntity.getPatient());
        JsonNode patchedPatientJson = JsonPatch.apply(patientPatch, patientJson);

        if (patchedPatientJson.equals(patientJson)) {
            return new LatestVersion<>(patientEntity, getLatestVersion(patientId));
        }

        Patient patchedPatient = objectMapper.convertValue(patchedPatientJson, Patient.class);

        patientEntity.setPatient(patchedPatient);
        try {
            patientEntity.addPatch(new PatientPatchEntity(objectMapper.writeValueAsString(patientPatch)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new LatestVersion<>(patientEntityRepository.save(patientEntity), getLatestVersion(patientId));
    }

    @Transactional(readOnly = true)
    public String diff(UUID patientId, UUID otherPatientId, LocalDateTime dateTime) {

        return diff(patientId, otherPatientId, dateTime, null, null);
    }

    @Transactional(readOnly = true)
    public String diff(UUID patientId, UUID otherPatientId, long version, long otherVersion) {

        return diff(patientId, otherPatientId, null, version, otherVersion);
    }

    @Transactional(readOnly = true)
    private String diff(UUID patientId, UUID otherPatientId, LocalDateTime dateTime, Long version, Long otherVersion) {

        PatientEntity patientEntity;
        PatientEntity otherPatientEntity;
        if (dateTime != null) {
            otherPatientEntity = getAsOfDateTime(otherPatientId, dateTime).getEntity();
            if (otherPatientId.equals(patientId)) {
                patientEntity = get(patientId).getEntity();
            } else {
                patientEntity = getAsOfDateTime(patientId, dateTime).getEntity();
            }
        } else {
            patientEntity = getAsOfVersion(patientId, version).getEntity();
            otherPatientEntity = getAsOfVersion(otherPatientId, otherVersion).getEntity();
        }

        JsonNode patientJson = objectMapper.valueToTree(patientEntity.getPatient());
        JsonNode otherPatientJson = objectMapper.valueToTree(otherPatientEntity.getPatient());
        JsonNode diff = JsonDiff.asJson(patientJson, otherPatientJson);

        try {
            return objectMapper.writeValueAsString(diff);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(UUID patientId) {

        patientEntityRepository.deleteById(patientId);
    }

    public void deleteSnapshot(UUID snapshotId) {

        patientSnapshotEntityRepository.deleteById(snapshotId);
    }

    public void deleteAll() {

        patientEntityRepository.deleteAllInBatch();
    }

    public void deleteAllSnapshots() {

        patientSnapshotEntityRepository.deleteAllInBatch();
    }
}
