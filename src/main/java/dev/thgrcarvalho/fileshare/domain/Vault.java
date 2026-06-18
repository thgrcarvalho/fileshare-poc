package dev.thgrcarvalho.fileshare.domain;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class Vault {

    private static final int SNIPPET_PADDING = 20;

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

    public List<SearchHit> search(String query, Cipher cipher) {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(cipher, "cipher");
        if (query.isBlank()) {
            return List.of();
        }
        String needle = query.toLowerCase(Locale.ROOT);
        List<SearchHit> hits = new ArrayList<>();
        for (StoredFile file : files.values()) {
            if (file.deleted()) {
                continue;
            }
            match(file, needle, cipher).ifPresent(hits::add);
        }
        return List.copyOf(hits);
    }

    private Optional<SearchHit> match(StoredFile file, String needle, Cipher cipher) {
        Set<MatchField> matchedOn = EnumSet.noneOf(MatchField.class);
        if (file.name().value().toLowerCase(Locale.ROOT).contains(needle)) {
            matchedOn.add(MatchField.NAME);
        }
        String content = cipher.decrypt(file.ciphertext()).text();
        int index = content.toLowerCase(Locale.ROOT).indexOf(needle);
        Optional<String> snippet = Optional.empty();
        if (index >= 0) {
            matchedOn.add(MatchField.CONTENT);
            snippet = Optional.of(snippet(content, index, needle.length()));
        }
        if (matchedOn.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SearchHit(file.name(), matchedOn, snippet));
    }

    private static String snippet(String content, int index, int matchLength) {
        int start = Math.max(0, index - SNIPPET_PADDING);
        int end = Math.min(content.length(), index + matchLength + SNIPPET_PADDING);
        if (start > 0 && Character.isLowSurrogate(content.charAt(start))) {
            start--;
        }
        if (end < content.length() && Character.isLowSurrogate(content.charAt(end))) {
            end++;
        }
        String core = content.substring(start, end).strip();
        String prefix = start > 0 ? "..." : "";
        String suffix = end < content.length() ? "..." : "";
        return prefix + core + suffix;
    }
}
