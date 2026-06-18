package dev.thgrcarvalho.fileshare.domain;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VaultTest {

    private static final VaultId SHARE = VaultId.of("share");

    private final RecordingCipher cipher = new RecordingCipher();

    @Test
    void savesAndReadsBackTheOriginalContent() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("notes.txt"), Bytes.ofText("hello"), cipher);

        assertEquals("hello", vault.read(FileName.of("notes.txt"), cipher).text());
    }

    @Test
    void encryptsContentAtRest() {
        Vault vault = new Vault(SHARE);
        Bytes plaintext = Bytes.ofText("top secret");
        vault.save(FileName.of("notes.txt"), plaintext, cipher);

        Bytes ciphertext = cipher.transform(plaintext);
        assertNotEquals(plaintext, ciphertext);
        assertEquals(List.of(plaintext), cipher.encrypted);

        vault.read(FileName.of("notes.txt"), cipher);
        assertEquals(List.of(ciphertext), cipher.decrypted);
    }

    @Test
    void listFilesReturnsActiveNamesInInsertionOrder() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("1"), cipher);
        vault.save(FileName.of("b.txt"), Bytes.ofText("2"), cipher);

        assertEquals(List.of(FileName.of("a.txt"), FileName.of("b.txt")), vault.listFiles());
    }

    @Test
    void deleteHidesTheFileFromListingAndReads() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("1"), cipher);
        vault.delete(FileName.of("a.txt"));

        assertTrue(vault.listFiles().isEmpty());
        assertThrows(UnknownFileException.class, () -> vault.read(FileName.of("a.txt"), cipher));
    }

    @Test
    void deleteUnknownFileThrows() {
        Vault vault = new Vault(SHARE);
        UnknownFileException ex = assertThrows(UnknownFileException.class, () -> vault.delete(FileName.of("ghost.txt")));
        assertEquals(FileName.of("ghost.txt"), ex.name());
    }

    @Test
    void deleteAlreadyDeletedFileThrows() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("1"), cipher);
        vault.delete(FileName.of("a.txt"));

        FileAlreadyDeletedException ex = assertThrows(FileAlreadyDeletedException.class, () -> vault.delete(FileName.of("a.txt")));
        assertEquals(FileName.of("a.txt"), ex.name());
    }

    @Test
    void restoreBringsBackTheOriginalContent() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("original"), cipher);
        vault.delete(FileName.of("a.txt"));

        vault.restore(FileName.of("a.txt"));

        assertEquals(List.of(FileName.of("a.txt")), vault.listFiles());
        assertEquals("original", vault.read(FileName.of("a.txt"), cipher).text());
    }

    @Test
    void restoreUnknownFileThrows() {
        Vault vault = new Vault(SHARE);
        assertThrows(UnknownFileException.class, () -> vault.restore(FileName.of("ghost.txt")));
    }

    @Test
    void restoreActiveFileThrows() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("1"), cipher);

        FileNotDeletedException ex = assertThrows(FileNotDeletedException.class, () -> vault.restore(FileName.of("a.txt")));
        assertEquals(FileName.of("a.txt"), ex.name());
    }

    @Test
    void savingAnExistingFileOverwritesItsContent() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("first"), cipher);
        vault.save(FileName.of("a.txt"), Bytes.ofText("second"), cipher);

        assertEquals(List.of(FileName.of("a.txt")), vault.listFiles());
        assertEquals("second", vault.read(FileName.of("a.txt"), cipher).text());
    }

    @Test
    void savingOverADeletedNameReactivatesWithNewContent() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("old"), cipher);
        vault.delete(FileName.of("a.txt"));

        vault.save(FileName.of("a.txt"), Bytes.ofText("new"), cipher);

        assertEquals("new", vault.read(FileName.of("a.txt"), cipher).text());
        assertThrows(FileNotDeletedException.class, () -> vault.restore(FileName.of("a.txt")));
    }

    @Test
    void searchMatchesByNameCaseInsensitively() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("Report.txt"), Bytes.ofText("body"), cipher);
        vault.save(FileName.of("notes.md"), Bytes.ofText("payload"), cipher);

        List<SearchHit> hits = vault.search("report", cipher);

        assertEquals(1, hits.size());
        SearchHit hit = hits.get(0);
        assertEquals(FileName.of("Report.txt"), hit.name());
        assertEquals(Set.of(MatchField.NAME), hit.matchedOn());
        assertTrue(hit.snippet().isEmpty());
    }

    @Test
    void searchMatchesByDecryptedContentWithSnippet() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("the quick brown fox jumps"), cipher);
        vault.save(FileName.of("b.txt"), Bytes.ofText("lazy dog"), cipher);

        List<SearchHit> hits = vault.search("BROWN", cipher);

        assertEquals(1, hits.size());
        SearchHit hit = hits.get(0);
        assertEquals(FileName.of("a.txt"), hit.name());
        assertEquals(Set.of(MatchField.CONTENT), hit.matchedOn());
        assertTrue(hit.snippet().orElseThrow().toLowerCase(Locale.ROOT).contains("brown"));
    }

    @Test
    void searchReportsBothFieldsWhenNameAndContentMatch() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("brown.txt"), Bytes.ofText("a brown fox"), cipher);

        SearchHit hit = vault.search("brown", cipher).get(0);

        assertEquals(Set.of(MatchField.NAME, MatchField.CONTENT), hit.matchedOn());
        assertTrue(hit.matchedName());
        assertTrue(hit.matchedContent());
    }

    @Test
    void contentSnippetIsTrimmedWithEllipsisAroundTheMatch() {
        Vault vault = new Vault(SHARE);
        String content = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa NEEDLE bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
        vault.save(FileName.of("big.txt"), Bytes.ofText(content), cipher);

        String snippet = vault.search("needle", cipher).get(0).snippet().orElseThrow();

        assertTrue(snippet.startsWith("..."));
        assertTrue(snippet.endsWith("..."));
        assertTrue(snippet.contains("NEEDLE"));
    }

    @Test
    void contentSnippetDoesNotSplitSurrogatePairs() {
        Vault vault = new Vault(SHARE);
        String content = "needle" + "a" + "😀".repeat(20);
        vault.save(FileName.of("emoji.txt"), Bytes.ofText(content), cipher);

        String snippet = vault.search("needle", cipher).get(0).snippet().orElseThrow();

        assertEquals(snippet, new String(snippet.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
    }

    @Test
    void searchReturnsAllMatchesInInsertionOrder() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("b-note.txt"), Bytes.ofText("shared term"), cipher);
        vault.save(FileName.of("a-note.txt"), Bytes.ofText("shared term"), cipher);

        List<SearchHit> hits = vault.search("shared", cipher);

        assertEquals(List.of(FileName.of("b-note.txt"), FileName.of("a-note.txt")),
                hits.stream().map(SearchHit::name).toList());
    }

    @Test
    void searchForABlankQueryReturnsNothing() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("anything"), cipher);

        assertTrue(vault.search("", cipher).isEmpty());
        assertTrue(vault.search("   ", cipher).isEmpty());
    }

    @Test
    void searchExcludesDeletedFiles() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("findme"), cipher);
        vault.delete(FileName.of("a.txt"));

        assertTrue(vault.search("findme", cipher).isEmpty());
    }

    @Test
    void readUnknownFileThrows() {
        Vault vault = new Vault(SHARE);
        UnknownFileException ex = assertThrows(UnknownFileException.class, () -> vault.read(FileName.of("ghost.txt"), cipher));
        assertEquals(FileName.of("ghost.txt"), ex.name());
    }

    private static final class RecordingCipher implements Cipher {

        private final List<Bytes> encrypted = new ArrayList<>();
        private final List<Bytes> decrypted = new ArrayList<>();

        @Override
        public Bytes encrypt(Bytes plaintext) {
            encrypted.add(plaintext);
            return transform(plaintext);
        }

        @Override
        public Bytes decrypt(Bytes ciphertext) {
            decrypted.add(ciphertext);
            return transform(ciphertext);
        }

        Bytes transform(Bytes input) {
            byte[] bytes = input.value();
            for (int index = 0; index < bytes.length; index++) {
                bytes[index] ^= 0x5A;
            }
            return Bytes.of(bytes);
        }
    }
}
