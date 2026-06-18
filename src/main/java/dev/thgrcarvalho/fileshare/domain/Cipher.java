package dev.thgrcarvalho.fileshare.domain;

public interface Cipher {

    Bytes encrypt(Bytes plaintext);

    Bytes decrypt(Bytes ciphertext);
}
