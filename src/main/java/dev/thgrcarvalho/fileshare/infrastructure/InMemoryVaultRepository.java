package dev.thgrcarvalho.fileshare.infrastructure;

import dev.thgrcarvalho.fileshare.domain.Vault;
import dev.thgrcarvalho.fileshare.domain.VaultId;
import dev.thgrcarvalho.fileshare.domain.VaultRepository;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryVaultRepository implements VaultRepository {

    private final Map<VaultId, Vault> vaults = new ConcurrentHashMap<>();

    @Override
    public void save(Vault vault) {
        Objects.requireNonNull(vault, "vault");
        vaults.put(vault.id(), vault);
    }

    @Override
    public Optional<Vault> findById(VaultId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(vaults.get(id));
    }
}
