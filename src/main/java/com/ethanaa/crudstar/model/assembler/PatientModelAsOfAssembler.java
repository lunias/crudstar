package com.ethanaa.crudstar.model.assembler;

import com.ethanaa.crudstar.controller.PatientController;
import com.ethanaa.crudstar.model.api.PatientModel;
import com.ethanaa.crudstar.model.api.Version;
import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PatientModelAsOfAssembler extends RepresentationModelAssemblerSupport<Version<PatientEntity>, PatientModel> {

    public PatientModelAsOfAssembler() {
        super(PatientController.class, PatientModel.class);
    }

    @Override
    public PatientModel toModel(Version<PatientEntity> patientVersion) {

        PatientModel patientModel = instantiateModel(patientVersion);

        BeanUtils.copyProperties(patientVersion.getEntity().getPatient(), patientModel);

        long nextVersion = patientVersion.getVersion() + 1;
        if (nextVersion > patientVersion.getLatestVersion()) {
            nextVersion = -1;
        }

        long previousVersion = patientVersion.getVersion() - 1;
        if (previousVersion < 1) {
            previousVersion = -1;
        }

        patientModel.add(linkTo(methodOn(PatientController.class)
                .getPatient(patientVersion.getEntity().getId(),
                        patientVersion.getAsOf())).withSelfRel());

        patientModel.add(linkTo(methodOn(PatientController.class)
                .getPatientPatches(patientVersion.getEntity().getId(),
                        patientVersion.getAsOf(), null, null)).withRel("patches"));

        if (previousVersion > 0) {
            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientVersion(patientVersion.getEntity().getId(),
                            previousVersion)).withRel("previousVersion"));

            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientVersionDiff(patientVersion.getEntity().getId(),
                            patientVersion.getEntity().getId(),
                            patientVersion.getVersion(),
                            previousVersion)).withRel("diffAgainstPreviousVersion"));
        }

        if (nextVersion > 0) {
            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientVersion(patientVersion.getEntity().getId(),
                            nextVersion)).withRel("nextVersion"));

            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientVersionDiff(patientVersion.getEntity().getId(),
                            patientVersion.getEntity().getId(),
                            patientVersion.getVersion(),
                            nextVersion)).withRel("diffAgainstNextVersion"));

            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientDiff(patientVersion.getEntity().getId(),
                            patientVersion.getEntity().getId(),
                            patientVersion.getAsOf())).withRel("diffAgainstLatestVersion"));

            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getLatestVersion(patientVersion.getEntity().getId())).withRel("latestVersion"));
        } else {
            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientDiff(patientVersion.getEntity().getId(),
                            null, patientVersion.getAsOf())).withRel("diff"));
        }

        return patientModel;
    }
}
