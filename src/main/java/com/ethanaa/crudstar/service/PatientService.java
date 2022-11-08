package com.ethanaa.crudstar.service;

import com.ethanaa.crudstar.model.api.ApiFilter;
import com.ethanaa.crudstar.model.api.exception.PatientNotFoundException;
import com.ethanaa.crudstar.model.persist.patient.Patient;
import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import com.ethanaa.crudstar.model.persist.patient.patch.PatientPatchEntity;
import com.ethanaa.crudstar.repository.PatientEntityRepository;
import com.ethanaa.crudstar.repository.PatientPatchEntityRepository;
import com.ethanaa.crudstar.repository.specification.PatientEntitySpecification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonPatch;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PatientService {

    private PatientEntityRepository patientEntityRepository;
    private PatientPatchEntityRepository patientPatchEntityRepository;

    private ObjectMapper objectMapper;

    @Autowired
    public PatientService(PatientEntityRepository patientEntityRepository,
                          PatientPatchEntityRepository patientPatchEntityRepository,
                          ObjectMapper objectMapper) {

        this.patientEntityRepository = patientEntityRepository;
        this.patientPatchEntityRepository = patientPatchEntityRepository;
        this.objectMapper = objectMapper;
    }

    public PatientEntity create(Patient patient) {

        PatientEntity patientEntity = new PatientEntity(patient);

        return patientEntityRepository.save(patientEntity);
    }

    public void create(Iterable<Patient> patients) {

        List<PatientEntity> patientEntities = new ArrayList<>();
        for (Patient patient : patients) {
            patientEntities.add(new PatientEntity(patient));
        }

        patientEntityRepository.saveAll(patientEntities);
    }

    public Page<PatientEntity> get(Pageable pageable, List<ApiFilter> filters) {

        if (filters != null && !filters.isEmpty()) {
            // sort handled by specification, create a dummy w/ just page info to send to repository
            PageRequest pageRequest = PageRequest.of(
                    pageable.getPageNumber(), pageable.getPageSize());
            PatientEntitySpecification specification = new PatientEntitySpecification(filters, pageable);
            return patientEntityRepository.findAll(specification, pageRequest);
        }

        List<Pair<Sort.Direction, String>> sorts = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            sorts.add(Pair.of(order.getDirection(), "patient->>'" + order.getProperty() + "'"));
        }
        sorts.add(Pair.of(Sort.Direction.DESC, "updated_at"));

        JpaSort jpaSort = null;
        for (Pair<Sort.Direction, String> sort : sorts) {
            if (jpaSort == null) {
                // TODO sort passed in directly (make safe)
                jpaSort = JpaSort.unsafe(sort.getFirst(), sort.getSecond());
            } else {
                jpaSort = jpaSort.andUnsafe(sort.getFirst(), sort.getSecond());
            }
        }

        PageRequest pageRequest = null;
        if (jpaSort != null) {
            pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), jpaSort);
        } else {
            pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
        }

        return patientEntityRepository.findAll(pageRequest);
    }

    public PatientEntity get(UUID patientId) {

        return patientEntityRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
    }

    public Page<PatientPatchEntity> getPatches(UUID patientId, Pageable pageable) {

        return patientPatchEntityRepository.findByPatientIdOrderByCreatedAtDesc(pageable, patientId);
    }

    public Page<PatientEntity> search(String query, Pageable pageable, List<ApiFilter> filters) {

        if (filters != null && !filters.isEmpty()) {
            // Cannot add filters to this native query via specification
            List<PatientEntity> patients = patientEntityRepository.search(query);

            // Apply filters after searching
            for (ApiFilter filter : filters) {

            }

            Comparator<PatientEntity> comparing = Comparator.comparing((pe) -> {
                return pe.getPatient().getFirstName();
            });
            // Apply sorts after filtering
            for (Sort.Order order : pageable.getSort()) {

            }

            // Build a page to return
            return new PageImpl<>(patients, pageable, patients.size());
        }

        List<Pair<Sort.Direction, String>> sorts = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            sorts.add(Pair.of(order.getDirection(), "patient->>'" + order.getProperty() + "'"));
        }
        sorts.add(Pair.of(Sort.Direction.DESC, "updated_at"));

        JpaSort jpaSort = null;
        for (Pair<Sort.Direction, String> sort : sorts) {
            if (jpaSort == null) {
                // TODO sort passed in directly (make safe)
                jpaSort = JpaSort.unsafe(sort.getFirst(), sort.getSecond());
            } else {
                jpaSort = jpaSort.andUnsafe(sort.getFirst(), sort.getSecond());
            }
        }

        PageRequest pageRequest = null;
        if (jpaSort != null) {
            pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), jpaSort);
        } else {
            pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
        }

        return patientEntityRepository.search(query, pageRequest);
    }

    public PatientEntity update(UUID patientId, Patient patient) {

        PatientEntity patientEntity = patientEntityRepository.findByIdWithLock(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        BeanUtils.copyProperties(patient, patientEntity.getPatient());

        return patientEntityRepository.save(patientEntity);
    }

    public PatientEntity patch(UUID patientId, JsonNode patientPatch) {

        PatientEntity patientEntity = patientEntityRepository.findByIdWithLock(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        JsonNode patientJson = objectMapper.valueToTree(patientEntity.getPatient());
        JsonNode patchedPatientJson = JsonPatch.apply(patientPatch, patientJson);

        Patient patchedPatient = objectMapper.convertValue(patchedPatientJson, Patient.class);

        patientEntity.setPatient(patchedPatient);
        try {
            patientEntity.addPatch(new PatientPatchEntity(objectMapper.writeValueAsString(patientPatch)));
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // TODO throw exception
            return null;
        }

        return patientEntityRepository.save(patientEntity);
    }

    public void delete(UUID patientId) {

        patientEntityRepository.deleteById(patientId);
    }

    public void deleteAll() {

        patientEntityRepository.deleteAllInBatch();
    }
}
