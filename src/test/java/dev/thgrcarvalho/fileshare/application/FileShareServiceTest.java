package dev.thgrcarvalho.fileshare.application;

import dev.thgrcarvalho.fileshare.domain.UnknownVaultException;
import dev.thgrcarvalho.fileshare.domain.VaultAlreadyExistsException;
import dev.thgrcarvalho.fileshare.domain.VaultId;
import dev.thgrcarvalho.fileshare.infrastructure.InMemoryVaultRepository;
import dev.thgrcarvalho.fileshare.infrastructure.XorCipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileShareServiceTest {

    private static final VaultId SHARE = VaultId.of("share");
    private static final VaultId TEAM = VaultId.of("team");

    private FileShareService service;

    @BeforeEach
    void setUp() {
        service = new FileShareService(new InMemoryVaultRepository(), new XorCipher(new byte[] {1, 2, 3, 4}));
    }

    @Test
    void createsAnEmptyVault() {
        service.create(SHARE);
        assertTrue(service.listFiles(SHARE).isEmpty());
    }

    @Test
    void rejectsCreatingAVaultThatAlreadyExists() {
        service.create(SHARE);
        VaultAlreadyExistsException ex = assertThrows(VaultAlreadyExistsException.class, () -> service.create(SHARE));
        assertEquals(SHARE, ex.id());
    }

    @Test
    void savesAndReadsBackBytes() {
        service.create(SHARE);
        service.save(SHARE, "notes.txt", text("hello world"));

        assertArrayEquals(text("hello world"), service.read(SHARE, "notes.txt"));
    }

    @Test
    void listsActiveFiles() {
        service.create(SHARE);
        service.save(SHARE, "a.txt", text("1"));
        service.save(SHARE, "b.txt", text("2"));

        assertEquals(List.of("a.txt", "b.txt"), service.listFiles(SHARE));
    }

    @Test
    void deletesAndRestoresAFile() {
        service.create(SHARE);
        service.save(SHARE, "a.txt", text("keep me"));
        service.delete(SHARE, "a.txt");
        assertTrue(service.listFiles(SHARE).isEmpty());

        service.restore(SHARE, "a.txt");
        assertEquals(List.of("a.txt"), service.listFiles(SHARE));
        assertArrayEquals(text("keep me"), service.read(SHARE, "a.txt"));
    }

    @Test
    void searchesByNameAndContent() {
        service.create(SHARE);
        service.save(SHARE, "groceries.txt", text("milk and eggs"));
        service.save(SHARE, "todo.txt", text("call the bank"));

        assertEquals(List.of("groceries.txt"), service.search(SHARE, "GROCERIES"));
        assertEquals(List.of("todo.txt"), service.search(SHARE, "bank"));
    }

    @Test
    void operatingOnAnUnknownVaultThrows() {
        UnknownVaultException ex = assertThrows(UnknownVaultException.class, () -> service.save(VaultId.of("ghost"), "a.txt", text("x")));
        assertEquals(VaultId.of("ghost"), ex.id());
    }

    @Test
    void vaultsAreIndependent() {
        service.create(SHARE);
        service.create(TEAM);
        service.save(SHARE, "a.txt", text("1"));

        assertEquals(List.of("a.txt"), service.listFiles(SHARE));
        assertTrue(service.listFiles(TEAM).isEmpty());
    }

    private static byte[] text(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
