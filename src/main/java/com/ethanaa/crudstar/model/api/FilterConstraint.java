package com.ethanaa.crudstar.model.api;

public class FilterConstraint {

    private String value;

    private String matchMode;

    public FilterConstraint() {

    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(String matchMode) {
        this.matchMode = matchMode;
    }
}
