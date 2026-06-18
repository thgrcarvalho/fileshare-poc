package dev.thgrcarvalho.fileshare.domain;

public final class FileNotDeletedException extends FileShareException {

    private final FileName name;

    public FileNotDeletedException(FileName name) {
        super("file is not deleted, nothing to restore: " + name);
        this.name = name;
    }

    public FileName name() {
        return name;
    }
}
