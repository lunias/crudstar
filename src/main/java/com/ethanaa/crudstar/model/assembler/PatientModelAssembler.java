package com.ethanaa.crudstar.model.assembler;

import com.ethanaa.crudstar.controller.PatientController;
import com.ethanaa.crudstar.model.api.LatestVersion;
import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import com.ethanaa.crudstar.model.api.PatientModel;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PatientModelAssembler extends RepresentationModelAssemblerSupport<LatestVersion<PatientEntity>, PatientModel> {

    public PatientModelAssembler() {
        super(PatientController.class, PatientModel.class);
    }

    @Override
    public PatientModel toModel(LatestVersion<PatientEntity> newPatientVersion) {

        PatientModel patientModel = instantiateModel(newPatientVersion);

        BeanUtils.copyProperties(newPatientVersion.getEntity().getPatient(), patientModel);

        long previousVersion = newPatientVersion.getVersion() - 1;
        if (previousVersion < 1) {
            previousVersion = -1;
        }

        patientModel.add(linkTo(methodOn(PatientController.class)
                .getPatient(newPatientVersion.getEntity().getId(),
                        null)).withSelfRel());

        patientModel.add(linkTo(methodOn(PatientController.class)
                .getPatientPatches(newPatientVersion.getEntity().getId(),
                        null, null, null)).withRel("patches"));

        if (previousVersion > 0) {
            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientVersion(newPatientVersion.getEntity().getId(),
                            previousVersion)).withRel("previousVersion"));

            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientVersionDiff(newPatientVersion.getEntity().getId(),
                            newPatientVersion.getEntity().getId(),
                            newPatientVersion.getVersion(),
                            previousVersion)).withRel("diffAgainstPreviousVersion"));
        }

        patientModel.add(linkTo(methodOn(PatientController.class)
                .getPatientDiff(newPatientVersion.getEntity().getId(), null, null)).withRel("diff"));

        return patientModel;
    }
}
