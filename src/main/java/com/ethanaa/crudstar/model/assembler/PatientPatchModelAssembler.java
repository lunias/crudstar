package com.ethanaa.crudstar.model.assembler;

import com.ethanaa.crudstar.controller.PatientPatchController;
import com.ethanaa.crudstar.model.api.PatchModel;
import com.ethanaa.crudstar.model.persist.patient.patch.PatientPatchEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class PatientPatchModelAssembler extends RepresentationModelAssemblerSupport<PatientPatchEntity, PatchModel> {

    public PatientPatchModelAssembler() {
        super(PatientPatchController.class, PatchModel.class);
    }

    @Override
    public PatchModel toModel(PatientPatchEntity entity) {

        PatchModel patchModel = instantiateModel(entity);

        BeanUtils.copyProperties(entity, patchModel);

        return patchModel;
    }
}
