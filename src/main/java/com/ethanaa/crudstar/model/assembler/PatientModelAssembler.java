package com.ethanaa.crudstar.model.assembler;

import com.ethanaa.crudstar.controller.PatientController;
import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import com.ethanaa.crudstar.model.api.PatientModel;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PatientModelAssembler extends RepresentationModelAssemblerSupport<PatientEntity, PatientModel> {

    public PatientModelAssembler() {
        super(PatientController.class, PatientModel.class);
    }

    @Override
    public PatientModel toModel(PatientEntity entity) {

        PatientModel patientModel = instantiateModel(entity);

        BeanUtils.copyProperties(entity.getPatient(), patientModel);

        patientModel.add(linkTo(methodOn(PatientController.class).getPatient(entity.getId())).withSelfRel());
        patientModel.add(linkTo(methodOn(PatientController.class).getPatientPatches(entity.getId(), null, null)).withRel("patches"));
        patientModel.add(linkTo(methodOn(PatientController.class).getPatientSuggestions(entity.getId())).withRel("suggestions"));

        return patientModel;
    }
}
