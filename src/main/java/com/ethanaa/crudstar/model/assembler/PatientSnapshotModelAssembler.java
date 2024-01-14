package com.ethanaa.crudstar.model.assembler;

import com.ethanaa.crudstar.controller.PatientController;
import com.ethanaa.crudstar.model.api.PatientSnapshotModel;
import com.ethanaa.crudstar.model.persist.patient.PatientSnapshotEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PatientSnapshotModelAssembler extends RepresentationModelAssemblerSupport<PatientSnapshotEntity, PatientSnapshotModel> {

    public PatientSnapshotModelAssembler() {
        super(PatientController.class, PatientSnapshotModel.class);
    }

    @Override
    public PatientSnapshotModel toModel(PatientSnapshotEntity entity) {

        PatientSnapshotModel patientSnapshotModel = instantiateModel(entity);

        BeanUtils.copyProperties(entity, patientSnapshotModel);

        patientSnapshotModel.add(linkTo(methodOn(PatientController.class)
                .getSnapshot(entity.getId())).withSelfRel());

        return patientSnapshotModel;
    }
}
