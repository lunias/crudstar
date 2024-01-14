package com.ethanaa.crudstar.model.api;

import java.time.LocalDateTime;

public class Version<T> {

    T entity;

    long version;
    long latestVersion;

    LocalDateTime asOf;

    public Version(T entity, long version, long latestVersion) {
        this.entity = entity;
        this.version = version;
        this.latestVersion = latestVersion;
    }

    public Version(T entity, long version, long latestVersion, LocalDateTime asOf) {
        this(entity, version, latestVersion);
        this.asOf = asOf;
    }

    public T getEntity() {
        return entity;
    }

    public long getVersion() {
        return version;
    }

    public long getLatestVersion() {
        return latestVersion;
    }

    public LocalDateTime getAsOf() {
        return asOf;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setLatestVersion(long latestVersion) {
        this.latestVersion = latestVersion;
    }

    public void setAsOf(LocalDateTime asOf) {
        this.asOf = asOf;
    }
}
