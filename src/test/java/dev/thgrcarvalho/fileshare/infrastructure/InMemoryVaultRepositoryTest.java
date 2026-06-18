package dev.thgrcarvalho.fileshare.infrastructure;

import dev.thgrcarvalho.fileshare.domain.Bytes;
import dev.thgrcarvalho.fileshare.domain.FileName;
import dev.thgrcarvalho.fileshare.domain.Vault;
import dev.thgrcarvalho.fileshare.domain.VaultId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryVaultRepositoryTest {

    private static final VaultId SHARE = VaultId.of("share");

    @Test
    void savedVaultIsFoundById() {
        InMemoryVaultRepository repository = new InMemoryVaultRepository();
        Vault vault = new Vault(SHARE);
        vault.save(FileName.of("a.txt"), Bytes.ofText("1"), new XorCipher(new byte[] {9}));
        repository.save(vault);

        Vault found = repository.findById(SHARE).orElseThrow();
        assertEquals(SHARE, found.id());
        assertEquals(1, found.listFiles().size());
    }

    @Test
    void findByIdIsEmptyForUnknownId() {
        InMemoryVaultRepository repository = new InMemoryVaultRepository();
        assertTrue(repository.findById(VaultId.of("nope")).isEmpty());
    }

    @Test
    void saveOverwritesTheVaultWithTheSameId() {
        InMemoryVaultRepository repository = new InMemoryVaultRepository();
        Vault vault = new Vault(SHARE);
        repository.save(vault);
        vault.save(FileName.of("a.txt"), Bytes.ofText("1"), new XorCipher(new byte[] {9}));
        repository.save(vault);

        assertEquals(1, repository.findById(SHARE).orElseThrow().listFiles().size());
    }
}
