package io.simplehex.map.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ActorRole {
    OWNER("owner"),
    GM("gm"),
    PLAYER("player");

    private final String value;

    ActorRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static ActorRole fromValue(String value) {
        for (ActorRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Unsupported actor role: " + value);
    }

    public boolean canEditMapContent() {
        return this == OWNER || this == GM;
    }
}