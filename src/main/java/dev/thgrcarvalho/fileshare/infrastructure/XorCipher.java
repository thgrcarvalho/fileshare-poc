package dev.thgrcarvalho.fileshare.infrastructure;

import dev.thgrcarvalho.fileshare.domain.Bytes;
import dev.thgrcarvalho.fileshare.domain.Cipher;

import java.util.Objects;

public final class XorCipher implements Cipher {

    private final byte[] key;

    public XorCipher(byte[] key) {
        Objects.requireNonNull(key, "key");
        if (key.length == 0) {
            throw new IllegalArgumentException("key must not be empty");
        }
        this.key = key.clone();
    }

    @Override
    public Bytes encrypt(Bytes plaintext) {
        Objects.requireNonNull(plaintext, "plaintext");
        return Bytes.of(apply(plaintext.value()));
    }

    @Override
    public Bytes decrypt(Bytes ciphertext) {
        Objects.requireNonNull(ciphertext, "ciphertext");
        return Bytes.of(apply(ciphertext.value()));
    }

    private byte[] apply(byte[] input) {
        byte[] output = new byte[input.length];
        for (int index = 0; index < input.length; index++) {
            output[index] = (byte) (input[index] ^ key[index % key.length]);
        }
        return output;
    }
}
