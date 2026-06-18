package dev.thgrcarvalho.fileshare.domain;

public final class FileAlreadyDeletedException extends FileShareException {

    private final FileName name;

    public FileAlreadyDeletedException(FileName name) {
        super("file is already deleted: " + name);
        this.name = name;
    }

    public FileName name() {
        return name;
    }
}
