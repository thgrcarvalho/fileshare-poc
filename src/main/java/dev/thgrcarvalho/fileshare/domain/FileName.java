package dev.thgrcarvalho.fileshare.domain;

import java.util.Objects;

public record FileName(String value) {

    public FileName {
        Objects.requireNonNull(value, "value");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("file name must not be blank");
        }
    }

    public static FileName of(String value) {
        return new FileName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
