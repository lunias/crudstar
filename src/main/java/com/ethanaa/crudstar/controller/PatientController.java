package com.ethanaa.crudstar.controller;

import com.ethanaa.crudstar.model.api.*;
import com.ethanaa.crudstar.model.assembler.PatientModelAssembler;
import com.ethanaa.crudstar.model.assembler.PatientPatchModelAssembler;
import com.ethanaa.crudstar.model.persist.patient.Patient;
import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import com.ethanaa.crudstar.model.persist.patient.patch.PatientPatchEntity;
import com.ethanaa.crudstar.service.PatientService;
import com.ethanaa.crudstar.service.SuggestionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patient")
public class PatientController {


    private PatientService patientService;
    private PatientModelAssembler patientModelAssembler;
    private PatientPatchModelAssembler patientPatchModelAssembler;
    private SuggestionService suggestionService;
    private ObjectMapper objectMapper;

    @Autowired
    public PatientController(PatientService patientService,
                             PatientModelAssembler patientModelAssembler,
                             PatientPatchModelAssembler patientPatchModelAssembler,
                             SuggestionService suggestionService,
                             ObjectMapper objectMapper) {

        this.patientService = patientService;
        this.patientModelAssembler = patientModelAssembler;
        this.patientPatchModelAssembler = patientPatchModelAssembler;
        this.suggestionService = suggestionService;
        this.objectMapper = objectMapper;
    }

    // CREATE

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> createPatient(@RequestBody Patient patient) {

        PatientEntity patientEntity = patientService.create(patient);

        PatientModel patientModel = patientModelAssembler.toModel(patientEntity);

        return ResponseEntity.created(patientModel.getRequiredLink("self").toUri()).body(patientModel);
    }

    // BATCH CREATE (JSON)

    @PostMapping(path = "/batch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createPatients(@RequestBody List<Patient> patients) {

        patientService.create(patients);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // RETRIEVE ALL (PAGED)

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PatientModel>> getPatients(
            Pageable pageable,
            @RequestParam(name = "filters", required = false) String filtersJson,
            PagedResourcesAssembler<PatientEntity> pagedResourcesAssembler) {

        List<ApiFilter> filters = null;
        if (StringUtils.hasText(filtersJson)) {
            try {
                filters = objectMapper.readValue(filtersJson, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        Page<PatientEntity> patientsPage = patientService.get(pageable, filters);

        PagedModel<PatientModel> patientModelsPage = pagedResourcesAssembler.toModel(patientsPage, patientModelAssembler);

        return ResponseEntity.ok(patientModelsPage);
    }

    // RETRIEVE ONE

    @GetMapping(path = "/{patientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> getPatient(@PathVariable UUID patientId) {

        PatientEntity patientEntity = patientService.get(patientId);

        PatientModel patientModel = patientModelAssembler.toModel(patientEntity);

        return ResponseEntity.ok(patientModel);
    }

    // RETRIEVE PATCHES

    @GetMapping(path = "/{patientId}/patches", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PatchModel>> getPatientPatches(
            @PathVariable UUID patientId,
            Pageable pageable,
            PagedResourcesAssembler<PatientPatchEntity> pagedResourcesAssembler) {

        Page<PatientPatchEntity> patientPatchPage = patientService.getPatches(patientId, pageable);

        PagedModel<PatchModel> patchModelsPage = pagedResourcesAssembler.toModel(patientPatchPage, patientPatchModelAssembler);

        return ResponseEntity.ok(patchModelsPage);
    }

    // RETRIEVE SUGGESTIONS

    @GetMapping(path = "/{patientId}/suggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionModel<SuggestionModel>> getPatientSuggestions(@PathVariable UUID patientId) {

        PatientEntity patientEntity = patientService.get(patientId);

        List<SuggestionModel> suggestions = suggestionService.get(patientEntity.getPatient());

        return ResponseEntity.ok(CollectionModel.of(suggestions));
    }

    // SEARCH ALL

    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PatientModel>> searchPatients(
            Pageable pageable,
            @RequestParam(name = "query", required = true) String query,
            @RequestParam(name = "filters", required = false) String filtersJson,
            PagedResourcesAssembler<PatientEntity> pagedResourcesAssembler) {

        List<ApiFilter> filters = null;
        if (StringUtils.hasText(filtersJson)) {
            try {
                filters = objectMapper.readValue(filtersJson, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        Page<PatientEntity> patientsPage = patientService.search(query, pageable, filters);

        PagedModel<PatientModel> patientModelsPage = pagedResourcesAssembler.toModel(patientsPage, patientModelAssembler);
        //patientModelsPage.removeLinks();

        return ResponseEntity.ok(patientModelsPage);
    }

    // SEARCH PATCHES


    // UPDATE ONE

    @PutMapping(path = "/{patientId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> updatePatient(@PathVariable UUID patientId, @RequestBody Patient patient) {

        PatientEntity patientEntity = patientService.update(patientId, patient);

        PatientModel patientModel = patientModelAssembler.toModel(patientEntity);

        return ResponseEntity.ok(patientModel);
    }

    // PATCH ONE

    @PatchMapping(path = "/{patientId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientModel> patchPatient(@PathVariable UUID patientId, @RequestBody JsonNode patientPatch) {

        PatientEntity patientEntity = patientService.patch(patientId, patientPatch);

        PatientModel patientModel = patientModelAssembler.toModel(patientEntity);

        return ResponseEntity.ok(patientModel);
    }


    // DELETE ONE

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID patientId) {

        patientService.delete(patientId);

        return ResponseEntity.ok().build();
    }

    // DELETE ALL

    @DeleteMapping
    public ResponseEntity<Void> deletePatients() {

        patientService.deleteAll();

        return ResponseEntity.ok().build();
    }

}
