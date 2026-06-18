package dev.thgrcarvalho.fileshare.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BytesTest {

    @Test
    void textRoundTrips() {
        assertEquals("hello", Bytes.ofText("hello").text());
    }

    @Test
    void equalsByContent() {
        assertEquals(Bytes.of(new byte[] {1, 2, 3}), Bytes.of(new byte[] {1, 2, 3}));
        assertEquals(Bytes.of(new byte[] {1, 2, 3}).hashCode(), Bytes.of(new byte[] {1, 2, 3}).hashCode());
    }

    @Test
    void differsByContent() {
        assertNotEquals(Bytes.of(new byte[] {1, 2, 3}), Bytes.of(new byte[] {1, 2, 4}));
    }

    @Test
    void copiesInputDefensively() {
        byte[] source = {1, 2, 3};
        Bytes bytes = Bytes.of(source);
        source[0] = 99;

        assertArrayEquals(new byte[] {1, 2, 3}, bytes.value());
    }

    @Test
    void copiesOutputDefensively() {
        Bytes bytes = Bytes.of(new byte[] {1, 2, 3});
        byte[] exposed = bytes.value();
        exposed[0] = 99;

        assertArrayEquals(new byte[] {1, 2, 3}, bytes.value());
    }

    @Test
    void reportsLength() {
        assertEquals(3, Bytes.of(new byte[] {1, 2, 3}).length());
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> Bytes.of(null));
    }
}
