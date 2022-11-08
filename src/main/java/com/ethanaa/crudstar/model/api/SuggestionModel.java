package com.ethanaa.crudstar.model.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.hateoas.RepresentationModel;

public class SuggestionModel extends RepresentationModel<SuggestionModel> implements Comparable<SuggestionModel> {

    @JsonIgnore
    private Long priority;

    private String field;

    private String suggestion;

    public SuggestionModel() {

    }

    public SuggestionModel(String field, String suggestion, long priority) {
        this.field = field;
        this.suggestion = suggestion;
        this.priority = priority;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public int compareTo(SuggestionModel o) {
        return priority.compareTo(o.priority);
    }
}
