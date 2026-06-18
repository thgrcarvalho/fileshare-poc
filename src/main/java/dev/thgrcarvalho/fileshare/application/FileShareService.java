package dev.thgrcarvalho.fileshare.application;

import dev.thgrcarvalho.fileshare.domain.Bytes;
import dev.thgrcarvalho.fileshare.domain.Cipher;
import dev.thgrcarvalho.fileshare.domain.FileName;
import dev.thgrcarvalho.fileshare.domain.UnknownVaultException;
import dev.thgrcarvalho.fileshare.domain.Vault;
import dev.thgrcarvalho.fileshare.domain.VaultAlreadyExistsException;
import dev.thgrcarvalho.fileshare.domain.VaultId;
import dev.thgrcarvalho.fileshare.domain.VaultRepository;

import java.util.List;
import java.util.Objects;

public final class FileShareService {

    private final VaultRepository repository;
    private final Cipher cipher;

    public FileShareService(VaultRepository repository, Cipher cipher) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.cipher = Objects.requireNonNull(cipher, "cipher");
    }

    public void create(VaultId id) {
        Objects.requireNonNull(id, "id");
        if (repository.findById(id).isPresent()) {
            throw new VaultAlreadyExistsException(id);
        }
        repository.save(new Vault(id));
    }

    public void save(VaultId id, String name, byte[] content) {
        Vault vault = require(id);
        vault.save(FileName.of(name), Bytes.of(content), cipher);
        repository.save(vault);
    }

    public void delete(VaultId id, String name) {
        Vault vault = require(id);
        vault.delete(FileName.of(name));
        repository.save(vault);
    }

    public void restore(VaultId id, String name) {
        Vault vault = require(id);
        vault.restore(FileName.of(name));
        repository.save(vault);
    }

    public byte[] read(VaultId id, String name) {
        return require(id).read(FileName.of(name), cipher).value();
    }

    public List<String> listFiles(VaultId id) {
        return require(id).listFiles().stream().map(FileName::value).toList();
    }

    public List<String> search(VaultId id, String query) {
        return require(id).search(query, cipher).stream().map(FileName::value).toList();
    }

    public Vault find(VaultId id) {
        return require(id);
    }

    private Vault require(VaultId id) {
        Objects.requireNonNull(id, "id");
        return repository.findById(id).orElseThrow(() -> new UnknownVaultException(id));
    }
}
