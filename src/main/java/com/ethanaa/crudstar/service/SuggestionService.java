package com.ethanaa.crudstar.service;

import com.ethanaa.crudstar.model.persist.patient.Patient;
import com.ethanaa.crudstar.model.api.SuggestionModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SuggestionService {

    public SuggestionService() {

    }

    public List<SuggestionModel> get(Patient patient) {

        List<SuggestionModel> suggestions = new ArrayList<>();

        if (!StringUtils.hasText(patient.getFirstName())) {
            suggestions.add(new SuggestionModel("firstName", "Enter a first name for the patient", 0));
        }

        if (!StringUtils.hasText(patient.getLastName())) {
            suggestions.add(new SuggestionModel("lastName", "Enter a last name for the patient", 0));
        }

        if (patient.getDateOfBirth() == null) {
            suggestions.add(new SuggestionModel("dateOfBirth", "Enter a date of birth for the patient", 1));
        }

        Collections.sort(suggestions);

        return suggestions;
    }
}
