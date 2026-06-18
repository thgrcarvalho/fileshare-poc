package dev.thgrcarvalho.fileshare.domain;

import java.util.Objects;

record StoredFile(FileName name, Bytes ciphertext, boolean deleted) {

    StoredFile {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(ciphertext, "ciphertext");
    }

    StoredFile asDeleted() {
        return new StoredFile(name, ciphertext, true);
    }

    StoredFile asActive() {
        return new StoredFile(name, ciphertext, false);
    }
}
