package com.ethanaa.crudstar.model.api;

import java.time.LocalDateTime;
import java.util.UUID;

public class Snapshot<T> extends Version<T> {

    UUID snapshotId;

    public Snapshot(T entity, long version, long latestVersion, UUID snapshotId) {
        super(entity, version, latestVersion);
        this.snapshotId = snapshotId;
    }

    public Snapshot(T entity, long version, long latestVersion, LocalDateTime asOf, UUID snapshotId) {
        super(entity, version, latestVersion, asOf);
        this.snapshotId = snapshotId;
    }

    public Snapshot(Version<T> version, UUID snapshotId) {
        this(version.getEntity(), version.getVersion(), version.getLatestVersion(), version.getAsOf(), snapshotId);
    }

    public UUID getSnapshotId() {
        return snapshotId;
    }
}
