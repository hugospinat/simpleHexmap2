package io.simplehex.map.realtime;

import io.simplehex.map.transport.MapSnapshotResponse;

public record SyncSnapshotMessage(String type, MapSnapshotResponse snapshot) {
}
