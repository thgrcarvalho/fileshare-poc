package dev.thgrcarvalho.fileshare.domain;

import java.util.Objects;

public record VaultId(String value) {

    public VaultId {
        Objects.requireNonNull(value, "value");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("vault id must not be blank");
        }
    }

    public static VaultId of(String value) {
        return new VaultId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
