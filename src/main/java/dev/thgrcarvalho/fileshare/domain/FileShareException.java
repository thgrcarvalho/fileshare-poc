package dev.thgrcarvalho.fileshare.domain;

public abstract sealed class FileShareException extends RuntimeException
        permits UnknownVaultException, VaultAlreadyExistsException, UnknownFileException,
                FileAlreadyDeletedException, FileNotDeletedException {

    protected FileShareException(String message) {
        super(message);
    }
}
