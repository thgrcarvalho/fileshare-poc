package dev.thgrcarvalho.fileshare.domain;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchHitTest {

    @Test
    void rejectsAHitThatMatchedNothing() {
        assertThrows(IllegalArgumentException.class,
                () -> new SearchHit(FileName.of("a.txt"), EnumSet.noneOf(MatchField.class), Optional.empty()));
    }

    @Test
    void exposesWhichFieldsMatched() {
        SearchHit hit = new SearchHit(FileName.of("a.txt"), Set.of(MatchField.CONTENT), Optional.of("...x..."));

        assertTrue(hit.matchedContent());
        assertFalse(hit.matchedName());
    }

    @Test
    void matchedFieldsAreImmutable() {
        Set<MatchField> source = EnumSet.of(MatchField.NAME);
        SearchHit hit = new SearchHit(FileName.of("a.txt"), source, Optional.empty());
        source.add(MatchField.CONTENT);

        assertEquals(Set.of(MatchField.NAME), hit.matchedOn());
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class,
                () -> new SearchHit(null, Set.of(MatchField.NAME), Optional.empty()));
    }

    @Test
    void rejectsASnippetOnANameOnlyHit() {
        assertThrows(IllegalArgumentException.class,
                () -> new SearchHit(FileName.of("a.txt"), Set.of(MatchField.NAME), Optional.of("...x...")));
    }

    @Test
    void rejectsAContentHitWithoutASnippet() {
        assertThrows(IllegalArgumentException.class,
                () -> new SearchHit(FileName.of("a.txt"), Set.of(MatchField.CONTENT), Optional.empty()));
    }
}
