package dev.thgrcarvalho.fileshare.domain;

public final class UnknownFileException extends FileShareException {

    private final FileName name;

    public UnknownFileException(FileName name) {
        super("no such file in the vault: " + name);
        this.name = name;
    }

    public FileName name() {
        return name;
    }
}
