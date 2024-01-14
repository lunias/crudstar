package com.ethanaa.crudstar.model.assembler;

import com.ethanaa.crudstar.controller.PatientController;
import com.ethanaa.crudstar.model.api.PatientModel;
import com.ethanaa.crudstar.model.api.Snapshot;
import com.ethanaa.crudstar.model.persist.patient.PatientEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PatientModelSnapshotAssembler extends RepresentationModelAssemblerSupport<Snapshot<PatientEntity>, PatientModel> {

    public PatientModelSnapshotAssembler() {
        super(PatientController.class, PatientModel.class);
    }

    @Override
    public PatientModel toModel(Snapshot<PatientEntity> patientSnapshot) {

        PatientModel patientModel = instantiateModel(patientSnapshot);

        BeanUtils.copyProperties(patientSnapshot.getEntity().getPatient(), patientModel);

        long nextVersion = patientSnapshot.getVersion() + 1;
        if (nextVersion > patientSnapshot.getLatestVersion()) {
            nextVersion = -1;
        }

        long previousVersion = patientSnapshot.getVersion() - 1;
        if (previousVersion < 1) {
            previousVersion = -1;
        }

        patientModel.add(linkTo(methodOn(PatientController.class)
                .getPatientSnapshot(patientSnapshot.getEntity().getId(),
                        patientSnapshot.getSnapshotId(), patientSnapshot.getAsOf())).withSelfRel());

        patientModel.add(linkTo(methodOn(PatientController.class)
                .getPatientSnapshotPatches(patientSnapshot.getEntity().getId(),
                        patientSnapshot.getSnapshotId(),
                        patientSnapshot.getAsOf(), null, null)).withRel("patches"));

        if (previousVersion > 0) {
            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientSnapshotVersion(patientSnapshot.getEntity().getId(),
                            patientSnapshot.getSnapshotId(),
                            previousVersion)).withRel("previousVersion"));

            // TODO snapshot version
            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientVersionDiff(patientSnapshot.getEntity().getId(),
                            patientSnapshot.getEntity().getId(),
                            patientSnapshot.getVersion(),
                            previousVersion)).withRel("diffAgainstPreviousVersion"));
        }

        if (nextVersion > 0) {
            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientSnapshotVersion(patientSnapshot.getEntity().getId(),
                            patientSnapshot.getSnapshotId(),
                            nextVersion)).withRel("nextVersion"));

            // TODO snapshot version
            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientVersionDiff(patientSnapshot.getEntity().getId(),
                            patientSnapshot.getEntity().getId(),
                            patientSnapshot.getVersion(),
                            nextVersion)).withRel("diffAgainstNextVersion"));

            // TODO snapshot version
            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientDiff(patientSnapshot.getEntity().getId(),
                            patientSnapshot.getEntity().getId(),
                            patientSnapshot.getAsOf())).withRel("diffAgainstLatestVersion"));

            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getLatestSnapshotVersion(patientSnapshot.getEntity().getId(),
                            patientSnapshot.getSnapshotId())).withRel("latestVersion"));
        } else {
            // TODO snapshot version
            patientModel.add(linkTo(methodOn(PatientController.class)
                    .getPatientDiff(patientSnapshot.getEntity().getId(),
                            null, patientSnapshot.getAsOf())).withRel("diff"));
        }

        return patientModel;
    }
}
