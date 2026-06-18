package dev.thgrcarvalho.fileshare.domain;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public record Bytes(byte[] value) {

    public Bytes {
        Objects.requireNonNull(value, "value");
        value = value.clone();
    }

    public static Bytes of(byte[] value) {
        return new Bytes(value);
    }

    public static Bytes ofText(String text) {
        Objects.requireNonNull(text, "text");
        return new Bytes(text.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte[] value() {
        return value.clone();
    }

    public int length() {
        return value.length;
    }

    public String text() {
        return new String(value, StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Bytes that && Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public String toString() {
        return "Bytes[" + value.length + "]";
    }
}
