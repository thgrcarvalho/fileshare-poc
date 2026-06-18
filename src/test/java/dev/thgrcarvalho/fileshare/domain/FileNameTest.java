package dev.thgrcarvalho.fileshare.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileNameTest {

    @Test
    void rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> FileName.of("   "));
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> FileName.of(null));
    }

    @Test
    void stripsSurroundingWhitespace() {
        assertEquals("a.txt", FileName.of("  a.txt  ").value());
    }

    @Test
    void isCaseSensitive() {
        assertNotEquals(FileName.of("Report.txt"), FileName.of("report.txt"));
    }

    @Test
    void toStringIsTheValue() {
        assertEquals("a.txt", FileName.of("a.txt").toString());
    }
}
