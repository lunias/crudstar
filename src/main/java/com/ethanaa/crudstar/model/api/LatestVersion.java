package com.ethanaa.crudstar.model.api;

public class LatestVersion<T> {

    T entity;

    long version;

    public LatestVersion(T entity, long version) {
        this.entity = entity;
        this.version = version;
    }

    public T getEntity() {
        return entity;
    }

    public long getVersion() {
        return version;
    }
}
