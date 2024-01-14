package com.ethanaa.crudstar.controller;

import com.ethanaa.crudstar.model.api.*;
import com.ethanaa.crudstar.model.assembler.*;
import com.ethanaa.crudstar.model.persist.patient.Patient;
import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import com.ethanaa.crudstar.model.persist.patient.PatientSnapshotEntity;
import com.ethanaa.crudstar.model.persist.patient.patch.PatientPatchEntity;
import com.ethanaa.crudstar.service.PatientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

// http://localhost:8080/swagger-ui/index.html#/
@Tag(name = "Patient", description = "Patient management APIs")
@RestController
@RequestMapping("/api/patient")
public class PatientController {


    private PatientService patientService;
    private PatientModelAssembler patientModelAssembler;
    private PatientModelAsOfAssembler patientModelAsOfAssembler;
    private PatientModelSnapshotAssembler patientModelSnapshotAssembler;
    private PatientSnapshotModelAssembler patientSnapshotModelAssembler;
    private PatientPatchModelAssembler patientPatchModelAssembler;
    private ObjectMapper objectMapper;

    @Autowired
    public PatientController(PatientService patientService,
                             PatientModelAssembler patientModelAssembler,
                             PatientModelAsOfAssembler patientModelAsOfAssembler,
                             PatientModelSnapshotAssembler patientModelSnapshotAssembler,
                             PatientSnapshotModelAssembler patientSnapshotModelAssembler,
                             PatientPatchModelAssembler patientPatchModelAssembler,
                             ObjectMapper objectMapper) {

        this.patientService = patientService;
        this.patientModelAssembler = patientModelAssembler;
        this.patientModelAsOfAssembler = patientModelAsOfAssembler;
        this.patientModelSnapshotAssembler = patientModelSnapshotAssembler;
        this.patientSnapshotModelAssembler = patientSnapshotModelAssembler;
        this.patientPatchModelAssembler = patientPatchModelAssembler;
        this.objectMapper = objectMapper;
    }

    @Operation(
            summary = "Create a Patient",
            description = "Create a Patient by providing the Patient as JSON",
            tags = { "patients", "create" })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> createPatient(@RequestBody Patient patient) {

        LatestVersion<PatientEntity> patientEntity = patientService.create(patient);

        PatientModel patientModel = patientModelAssembler.toModel(patientEntity);

        return ResponseEntity.created(patientModel.getRequiredLink("self").toUri()).body(patientModel);
    }

    @Operation(
            summary = "Create a Patient for a Snapshot",
            description = "Create a Patient for a given snapshot by providing the Snapshot UUID and the Patient as JSON",
            tags = { "patients", "snapshot", "create" })
    @PostMapping(path = "/snapshot/{snapshotId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> createSnapshotPatient(@RequestBody Patient patient, @PathVariable UUID snapshotId) {

        Snapshot<PatientEntity> patientEntity = patientService.create(patient, snapshotId);

        PatientModel patientModel = patientModelSnapshotAssembler.toModel(patientEntity);

        return ResponseEntity.created(patientModel.getRequiredLink("self").toUri()).body(patientModel);
    }

    @Operation(
            summary = "Create multiple Patients",
            description = "Create multiple Patients by providing a list of the Patients as JSON",
            tags = { "patients", "create", "batch" })
    @PostMapping(path = "/batch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createPatients(@RequestBody List<Patient> patients) {

        patientService.create(patients);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Create a Snapshot",
            description = "Create a Snapshot of all Patients by providing the DateTime to take a Snapshot as of",
            tags = { "patients", "snapshot", "create", "asOf" })
    @PostMapping(path = "/snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientSnapshotEntity> createSnapshot(
            @RequestParam(name = "asOf", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf) {

        if (asOf == null) {
            asOf = LocalDateTime.now();
        }

        PatientSnapshotEntity patientSnapshotEntity = patientService.createSnapshot(asOf);

        return ResponseEntity.status(HttpStatus.CREATED).body(patientSnapshotEntity);
    }

    @Operation(
            summary = "Get Snapshots",
            description = "Get a Page of all Snapshots",
            tags = { "patients", "snapshot", "get", "pageable", "asOf" })
    @GetMapping(path = "/snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PatientSnapshotModel>> getSnapshots(
            Pageable pageable,
            @RequestParam(name = "asOf", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf,
            PagedResourcesAssembler<PatientSnapshotEntity> pagedResourcesAssembler) {

        if (asOf == null) {
            asOf = LocalDateTime.now();
        }

        Page<PatientSnapshotEntity> patientSnapshotEntityPage = patientService.getSnapshots(pageable, asOf);

        return ResponseEntity.ok(pagedResourcesAssembler.toModel(patientSnapshotEntityPage, patientSnapshotModelAssembler));
    }

    @Operation(
            summary = "Get Patients for a Snapshot",
            description = "Get a Page of all Patients as seen by the Snapshot by providing the Snapshot UUID",
            tags = { "patients", "snapshot", "get", "pageable", "asOf" })
    @GetMapping(path = "/snapshot/{snapshotId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PatientModel>> getPatientSnapshots(
            Pageable pageable,
            @PathVariable UUID snapshotId,
            @RequestParam(name = "asOf", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf,
            PagedResourcesAssembler<Snapshot<PatientEntity>> pagedResourcesAssemblerSnapshot) {

        Page<Snapshot<PatientEntity>> patientsPage = patientService.getSnapshot(pageable, snapshotId, asOf);

        PagedModel<PatientModel> patientModelsPage = pagedResourcesAssemblerSnapshot
                .toModel(patientsPage, patientModelSnapshotAssembler);

        if (asOf != null) {
            for (Link pageLink : patientModelsPage.getLinks()) {
                if (!pageLink.getHref().contains("asOf=")) {
                    patientModelsPage.mapLink(pageLink.getRel(), link -> link.withHref(link.getHref() + "&asOf=" + asOf));
                }
            }
        }

        return ResponseEntity.ok(patientModelsPage);
    }

    @Operation(
            summary = "Get Patients",
            description = "Get a Page of all Patients",
            tags = { "patients", "get", "pageable", "queryable", "asOf" })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PatientModel>> getPatients(
            Pageable pageable,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "filters", required = false) String filtersJson,
            @RequestParam(name = "asOf", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf,
            PagedResourcesAssembler<LatestVersion<PatientEntity>> pagedResourcesAssembler,
            PagedResourcesAssembler<Version<PatientEntity>> pagedResourcesAssemblerAsOf) {

        if (asOf != null) {
            // TODO some way to apply query and filters to asOf results?
            Page<Version<PatientEntity>> patientsPage = patientService.getAsOfDateTime(pageable, asOf);

            PagedModel<PatientModel> patientModelsPage = pagedResourcesAssemblerAsOf
                    .toModel(patientsPage, patientModelAsOfAssembler);

            for (Link pageLink : patientModelsPage.getLinks()) {
                if (!pageLink.getHref().contains("asOf=")) {
                    patientModelsPage.mapLink(pageLink.getRel(), link -> link.withHref(link.getHref() + "&asOf=" + asOf));
                }
            }

            return ResponseEntity.ok(patientModelsPage);
        }

        List<ApiFilter> filters = new ArrayList<>();
        if (StringUtils.hasText(filtersJson)) {
            try {
                filters = objectMapper.readValue(filtersJson, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        Page<LatestVersion<PatientEntity>> patientsPage = patientService.get(pageable, query, filters);

        PagedModel<PatientModel> patientModelsPage = pagedResourcesAssembler.toModel(patientsPage, patientModelAssembler);

        return ResponseEntity.ok(patientModelsPage);
    }

    @Operation(
            summary = "Get a Patient",
            description = "Get a Patient by providing the Patient UUID",
            tags = { "patients", "get", "asOf" })
    @GetMapping(path = "/{patientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> getPatient(@PathVariable UUID patientId,
                                                   @RequestParam(name = "asOf", required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                   LocalDateTime asOf) {

        if (asOf != null) {
            Version<PatientEntity> patientEntity = patientService.getAsOfDateTime(patientId, asOf);

            return ResponseEntity.ok(patientModelAsOfAssembler.toModel(patientEntity));
        }

        LatestVersion<PatientEntity> patientEntity = patientService.get(patientId);

        return ResponseEntity.ok(patientModelAssembler.toModel(patientEntity));
    }

    @Operation(
            summary = "Get a Snapshot",
            description = "Get a Snapshot by providing the Snapshot UUID",
            tags = { "patients", "get", "snapshot" })
    @GetMapping(path = "/snapshot/{snapshotId}/snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientSnapshotModel> getSnapshot(@PathVariable UUID snapshotId) {

        PatientSnapshotEntity patientSnapshotEntity = patientService.getSnapshot(snapshotId);

        return ResponseEntity.ok(patientSnapshotModelAssembler.toModel(patientSnapshotEntity));
    }

    @Operation(
            summary = "Get a Patient for a Snapshot",
            description = "Get a Patient as seen by the Snapshot by providing the Patient UUID and the Snapshot UUID",
            tags = { "patients", "snapshot", "get", "asOf" })
    @GetMapping(path = "/{patientId}/snapshot/{snapshotId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> getPatientSnapshot(@PathVariable UUID patientId,
                                                           @PathVariable UUID snapshotId,
                                                           @RequestParam(name = "asOf", required = false)
                                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                               LocalDateTime asOf) {

        Snapshot<PatientEntity> patientEntityVersion = patientService.getSnapshot(patientId, snapshotId, asOf);

        return ResponseEntity.ok(patientModelSnapshotAssembler.toModel(patientEntityVersion));
    }

    @Operation(
            summary = "Get a Patient version",
            description = "Get a Patient as of some version by providing the Patient UUID and the version number",
            tags = { "patients", "get", "version" })
    @GetMapping(path = "/{patientId}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> getPatientVersion(@PathVariable UUID patientId, @PathVariable long version) {

        Version<PatientEntity> patientEntity = patientService.getAsOfVersion(patientId, version);

        return ResponseEntity.ok(patientModelAsOfAssembler.toModel(patientEntity));
    }

    @Operation(
            summary = "Get a Patient version for a Snapshot",
            description = "Get a Patient as seen by the Snapshot and as of some version by providing the Patient UUID, " +
                    "the Snapshot UUID, and the version number",
            tags = { "patients", "snapshot", "get", "version" })
    @GetMapping(path = "/{patientId}/snapshot/{snapshotId}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> getPatientSnapshotVersion(@PathVariable UUID patientId,
                                                                  @PathVariable UUID snapshotId,
                                                                  @PathVariable long version) {

        Snapshot<PatientEntity> patientEntity = patientService.getSnapshot(patientId, snapshotId, version);

        return ResponseEntity.ok(patientModelSnapshotAssembler.toModel(patientEntity));
    }

    @Operation(
            summary = "Get all Patient versions",
            description = "Get a Patient as of every version by providing the Patient UUID",
            tags = { "patients", "get", "version" })
    @GetMapping(path = "/{patientId}/version", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionModel<PatientModel>> getPatientVersions(@PathVariable UUID patientId) {

        Map<String, List<Version<PatientEntity>>> patientVersions =
                patientService.getVersions(Collections.singletonList(patientId));

        return ResponseEntity.ok(
                patientModelAsOfAssembler.toCollectionModel(patientVersions.get(patientId.toString())));
    }

    @Operation(
            summary = "Get all Patient versions for a Snapshot",
            description = "Get a Patient as seen by the Snapshot as of every version by providing the Patient UUID " +
                    "and the Snapshot UUID",
            tags = { "patients", "snapshot", "get", "version" })
    @GetMapping(path = "/{patientId}/snapshot/{snapshotId}/version", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionModel<PatientModel>> getPatientSnapshotVersions(@PathVariable UUID patientId,
                                                                                    @PathVariable UUID snapshotId) {

        List<Snapshot<PatientEntity>> patientVersions = patientService.getSnapshotVersions(patientId, snapshotId);

        return ResponseEntity.ok(
                patientModelSnapshotAssembler.toCollectionModel(patientVersions));
    }

    @Operation(
            summary = "Get the diff between Patients",
            description = "Get the diff between Patients by providing the Patient UUIDs",
            tags = { "patients", "get", "diff", "asOf" })
    @GetMapping(path = "/{patientId}/diff/{otherPatientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPatientDiff(@PathVariable UUID patientId,
                                                 @PathVariable UUID otherPatientId,
                                                 @RequestParam(name = "asOf", required = false)
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf) {

        String patientDiff = patientService.diff(patientId, otherPatientId, asOf);

        return ResponseEntity.ok(patientDiff);
    }

    @Operation(
            summary = "Get the diff between Patient versions",
            description = "Get the diff between Patient versions by providing the Patient UUIDs and version numbers",
            tags = { "patients", "get", "diff", "version" })
    @GetMapping(path = "/{patientId}/version/{version}/diff/{otherPatientId}/version/{otherVersion}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPatientVersionDiff(@PathVariable UUID patientId,
                                                        @PathVariable UUID otherPatientId,
                                                        @PathVariable long version,
                                                        @PathVariable long otherVersion) {

        String patientDiff = patientService.diff(patientId, otherPatientId, version, otherVersion);

        return ResponseEntity.ok(patientDiff);
    }

    @Operation(
            summary = "Get the latest Patient version",
            description = "Get the latest Patient version number by providing the Patient UUID",
            tags = { "patients", "get", "version" })
    @GetMapping(path = "/{patientId}/latestVersion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> getLatestVersion(@PathVariable UUID patientId) {

        return ResponseEntity.ok(patientService.getLatestVersion(patientId));
    }

    @Operation(
            summary = "Get the latest Patient version for a Snapshot",
            description = "Get the latest Patient version number as seen by the Snapshot by providing the Patient UUID and " +
                    "the Snapshot UUID",
            tags = { "patients", "snapshot", "get", "version" })
    @GetMapping(path = "/{patientId}/snapshot/{snapshotId}/latestVersion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> getLatestSnapshotVersion(@PathVariable UUID patientId, @PathVariable UUID snapshotId) {

        return ResponseEntity.ok(patientService.getLatestVersion(patientId, snapshotId));
    }

    @Operation(
            summary = "Get a Patient's Patches",
            description = "Get the Patches for a Patient by providing the Patient UUID",
            tags = { "patients", "get", "patches", "pageable", "asOf"})
    @GetMapping(path = "/{patientId}/patches", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PatchModel>> getPatientPatches(
            @PathVariable UUID patientId,
            @RequestParam(name = "asOf", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf,
            Pageable pageable,
            PagedResourcesAssembler<PatientPatchEntity> pagedResourcesAssembler) {

        Page<PatientPatchEntity> patientPatchPage;
        if (asOf != null) {
            patientPatchPage = patientService.getPatches(pageable, patientId, asOf);
        } else {
            patientPatchPage = patientService.getPatches(pageable, patientId);
        }

        PagedModel<PatchModel> patchModelsPage = pagedResourcesAssembler
                .toModel(patientPatchPage, patientPatchModelAssembler);

        return ResponseEntity.ok(patchModelsPage);
    }

    @Operation(
            summary = "Get a Patient's Patches for a Snapshot",
            description = "Get the Patches for a Patient as seen by the Snapshot by providing the Patient UUID and the " +
                    "Snapshot UUID",
            tags = { "patients", "get", "patches", "snapshot", "pageable", "asOf" })
    @GetMapping(path = "/{patientId}/snapshot/{snapshotId}/patches", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PatchModel>> getPatientSnapshotPatches(
            @PathVariable UUID patientId,
            @PathVariable UUID snapshotId,
            @RequestParam(name = "asOf", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf,
            Pageable pageable,
            PagedResourcesAssembler<PatientPatchEntity> pagedResourcesAssembler) {

        Page<PatientPatchEntity> patientPatchPage;
        if (asOf != null) {
            patientPatchPage = patientService.getSnapshotPatches(pageable, patientId, asOf, snapshotId);
        } else {
            patientPatchPage = patientService.getSnapshotPatches(pageable, patientId, snapshotId);
        }

        PagedModel<PatchModel> patchModelsPage = pagedResourcesAssembler
                .toModel(patientPatchPage, patientPatchModelAssembler);

        return ResponseEntity.ok(patchModelsPage);
    }

    @Operation(
            summary = "Search Patients",
            description = "Get a Page of all Patients that match the search query",
            tags = { "patients", "get", "pageable", "queryable" })
    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PatientModel>> searchPatients(
            Pageable pageable,
            @RequestParam(name = "query", required = true) String query,
            @RequestParam(name = "filters", required = false) String filtersJson,
            PagedResourcesAssembler<LatestVersion<PatientEntity>> pagedResourcesAssembler) {

        List<ApiFilter> filters = new ArrayList<>();
        if (StringUtils.hasText(filtersJson)) {
            try {
                filters = objectMapper.readValue(filtersJson, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        Page<LatestVersion<PatientEntity>> patientsPage = patientService.search(query, pageable, filters);

        PagedModel<PatientModel> patientModelsPage = pagedResourcesAssembler.toModel(patientsPage, patientModelAssembler);

        return ResponseEntity.ok(patientModelsPage);
    }

    @Operation(
            summary = "Update a Patient",
            description = "Update a Patient by providing the Patient UUID and the Patient as JSON",
            tags = { "patients", "update" })
    @PutMapping(path = "/{patientId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> updatePatient(@PathVariable UUID patientId, @RequestBody Patient patient) {

        LatestVersion<PatientEntity> patientEntity = patientService.update(patientId, patient);

        PatientModel patientModel = patientModelAssembler.toModel(patientEntity);

        return ResponseEntity.ok(patientModel);
    }

    @Operation(
            summary = "Update a Patient for a Snapshot",
            description = "Update a Patient as seen by the Snapshot by providing the Patient UUID, the Snapshot UUID, " +
                    "and the Patient as JSON",
            tags = { "patients", "update", "snapshot" })
    @PutMapping(path = "/{patientId}/snapshot/{snapshotId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> updatePatientSnapshot(
            @PathVariable UUID patientId, @PathVariable UUID snapshotId, @RequestBody Patient patient) {

        Snapshot<PatientEntity> patientEntity = patientService.update(patientId, patient, snapshotId);

        PatientModel patientModel = patientModelSnapshotAssembler.toModel(patientEntity);

        return ResponseEntity.ok(patientModel);
    }

    @Operation(
            summary = "Patch a Patient",
            description = "Patch a Patient by providing the Patient UUID and the Patient Patch as JSON",
            tags = { "patients", "update", "patches" })
    @PatchMapping(path = "/{patientId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> patchPatient(@PathVariable UUID patientId, @RequestBody JsonNode patientPatch) {

        LatestVersion<PatientEntity> patientEntity = patientService.patch(patientId, patientPatch);

        PatientModel patientModel = patientModelAssembler.toModel(patientEntity);

        return ResponseEntity.ok(patientModel);
    }

    @Operation(
            summary = "Delete a Patient",
            description = "Delete a Patient by providing the Patient UUID",
            tags = { "patients", "delete" })
    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID patientId) {

        patientService.delete(patientId);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Delete a Snapshot",
            description = "Delete a Snapshot by providing the Snapshot UUID",
            tags = { "patients", "snapshot", "delete" })
    @DeleteMapping("/snapshot/{snapshotId}")
    public ResponseEntity<Void> deleteSnapshot(@PathVariable UUID snapshotId) {

        patientService.deleteSnapshot(snapshotId);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Delete all Patients",
            description = "Delete all Patients",
            tags = { "patients", "delete", "batch" })
    @DeleteMapping
    public ResponseEntity<Void> deletePatients() {

        patientService.deleteAll();

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Delete all Snapshots",
            description = "Delete all Snapshots",
            tags = { "patients", "snapshot", "delete", "batch" })
    @DeleteMapping("/snapshot")
    public ResponseEntity<Void> deleteSnapshots() {

        patientService.deleteAllSnapshots();

        return ResponseEntity.ok().build();
    }

}
