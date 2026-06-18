package dev.thgrcarvalho.fileshare.domain;

import java.util.Optional;

public interface VaultRepository {

    void save(Vault vault);

    Optional<Vault> findById(VaultId id);
}
