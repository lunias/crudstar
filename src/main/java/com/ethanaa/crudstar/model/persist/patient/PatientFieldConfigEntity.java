package com.ethanaa.crudstar.model.persist.patient;

import com.ethanaa.crudstar.model.persist.UUIDEntity;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table
@TypeDef(name = "json", typeClass = JsonType.class)
public class PatientFieldConfigEntity extends UUIDEntity {

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private String config;

    public PatientFieldConfigEntity() {

    }

    public PatientFieldConfigEntity(String config) {

        this.config = config;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
