package dev.thgrcarvalho.fileshare.domain;

public final class UnknownVaultException extends FileShareException {

    private final VaultId id;

    public UnknownVaultException(VaultId id) {
        super("no vault found with id " + id);
        this.id = id;
    }

    public VaultId id() {
        return id;
    }
}
