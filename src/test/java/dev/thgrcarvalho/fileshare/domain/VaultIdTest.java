package dev.thgrcarvalho.fileshare.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VaultIdTest {

    @Test
    void rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> VaultId.of("  "));
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> VaultId.of(null));
    }

    @Test
    void stripsSurroundingWhitespace() {
        assertEquals("share", VaultId.of("  share  ").value());
    }

    @Test
    void toStringIsTheValue() {
        assertEquals("share", VaultId.of("share").toString());
    }
}
