package dev.thgrcarvalho.fileshare.domain;

public final class VaultAlreadyExistsException extends FileShareException {

    private final VaultId id;

    public VaultAlreadyExistsException(VaultId id) {
        super("a vault already exists with id " + id);
        this.id = id;
    }

    public VaultId id() {
        return id;
    }
}
