package dev.thgrcarvalho.fileshare.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class Vault {

    private final VaultId id;
    private final Map<FileName, StoredFile> files = new LinkedHashMap<>();

    public Vault(VaultId id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public VaultId id() {
        return id;
    }

    public void save(FileName name, Bytes plaintext, Cipher cipher) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(plaintext, "plaintext");
        Objects.requireNonNull(cipher, "cipher");
        files.put(name, new StoredFile(name, cipher.encrypt(plaintext), false));
    }

    public void delete(FileName name) {
        Objects.requireNonNull(name, "name");
        StoredFile file = files.get(name);
        if (file == null) {
            throw new UnknownFileException(name);
        }
        if (file.deleted()) {
            throw new FileAlreadyDeletedException(name);
        }
        files.put(name, file.asDeleted());
    }

    public void restore(FileName name) {
        Objects.requireNonNull(name, "name");
        StoredFile file = files.get(name);
        if (file == null) {
            throw new UnknownFileException(name);
        }
        if (!file.deleted()) {
            throw new FileNotDeletedException(name);
        }
        files.put(name, file.asActive());
    }

    public Bytes read(FileName name, Cipher cipher) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(cipher, "cipher");
        StoredFile file = files.get(name);
        if (file == null || file.deleted()) {
            throw new UnknownFileException(name);
        }
        return cipher.decrypt(file.ciphertext());
    }

    public List<FileName> listFiles() {
        return files.values().stream()
                .filter(file -> !file.deleted())
                .map(StoredFile::name)
                .toList();
    }

    public List<FileName> search(String query, Cipher cipher) {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(cipher, "cipher");
        String needle = query.toLowerCase(Locale.ROOT);
        return files.values().stream()
                .filter(file -> !file.deleted())
                .filter(file -> matches(file, needle, cipher))
                .map(StoredFile::name)
                .toList();
    }

    private boolean matches(StoredFile file, String needle, Cipher cipher) {
        if (file.name().value().toLowerCase(Locale.ROOT).contains(needle)) {
            return true;
        }
        return cipher.decrypt(file.ciphertext()).text().toLowerCase(Locale.ROOT).contains(needle);
    }
}
