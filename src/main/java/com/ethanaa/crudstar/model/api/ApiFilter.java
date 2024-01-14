package com.ethanaa.crudstar.model.api;

import java.util.List;

public class ApiFilter {

    private String key;

    private String operator;

    private List<FilterConstraint> constraints;

    public ApiFilter() {

    }

    public ApiFilter(String key, String operator, List<FilterConstraint> constraints) {
        this.key = key;
        this.operator = operator;
        this.constraints = constraints;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<FilterConstraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<FilterConstraint> constraints) {
        this.constraints = constraints;
    }
}
