package dev.thgrcarvalho.fileshare.infrastructure;

import dev.thgrcarvalho.fileshare.domain.Bytes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XorCipherTest {

    @Test
    void decryptUndoesEncrypt() {
        XorCipher cipher = new XorCipher(new byte[] {7, 11, 13});
        Bytes plaintext = Bytes.ofText("the quick brown fox");

        Bytes ciphertext = cipher.encrypt(plaintext);
        assertEquals(plaintext, cipher.decrypt(ciphertext));
    }

    @Test
    void ciphertextDiffersFromPlaintext() {
        XorCipher cipher = new XorCipher(new byte[] {42});
        Bytes plaintext = Bytes.ofText("secret");

        assertNotEquals(plaintext, cipher.encrypt(plaintext));
    }

    @Test
    void encryptionIsDeterministic() {
        XorCipher cipher = new XorCipher(new byte[] {1, 2, 3});
        Bytes plaintext = Bytes.ofText("same input");

        assertEquals(cipher.encrypt(plaintext), cipher.encrypt(plaintext));
    }

    @Test
    void handlesEmptyContent() {
        XorCipher cipher = new XorCipher(new byte[] {5});
        Bytes empty = Bytes.ofText("");

        assertEquals(empty, cipher.decrypt(cipher.encrypt(empty)));
    }

    @Test
    void rejectsEmptyKey() {
        assertThrows(IllegalArgumentException.class, () -> new XorCipher(new byte[0]));
    }

    @Test
    void rejectsNullKey() {
        assertThrows(NullPointerException.class, () -> new XorCipher(null));
    }
}
