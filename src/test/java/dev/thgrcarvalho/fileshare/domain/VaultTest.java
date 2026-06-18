package dev.thgrcarvalho.fileshare.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
        vault.save(FileName.of("notes.md"), Bytes.ofText("body"), cipher);

        assertEquals(List.of(FileName.of("Report.txt")), vault.search("report", cipher));
    }

    @Test
    void searchMatchesByDecryptedContent() {
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("the quick brown fox"), cipher);
        vault.save(FileName.of("b.txt"), Bytes.ofText("lazy dog"), cipher);

        assertEquals(List.of(FileName.of("a.txt")), vault.search("BROWN", cipher));
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
