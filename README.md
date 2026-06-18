# fileshare-poc

OOAD exercise: a **file share** with **encryption at rest**. You keep named vaults, and on each one you save files, read them back, soft-delete and restore them, list them, and search them by name or content. Files are **encrypted when saved and decrypted when read** — plaintext is never what sits in storage.

Pure Java 21 + JUnit 5. No external dependencies.

## Caller API

```java
FileShareService files = new FileShareService(new InMemoryVaultRepository(), new XorCipher(secretKey));
VaultId share = VaultId.of("team-share");

files.create(share);
files.save(share, "report.txt", "Q2 revenue up 12%".getBytes(UTF_8));   // stored encrypted
files.save(share, "notes.md", "remember to call the bank".getBytes(UTF_8));

files.listFiles(share);                                // [report.txt, notes.md]
files.search(share, "revenue");                        // [report.txt]  — matches decrypted content
new String(files.read(share, "report.txt"), UTF_8);    // "Q2 revenue up 12%"

files.delete(share, "notes.md");                       // soft delete (recoverable)
files.listFiles(share);                                // [report.txt]
files.restore(share, "notes.md");                      // back, with its original content
```

Reading, deleting, or restoring a file that isn't there is rejected, as is deleting an already-deleted file or restoring one that isn't deleted. Creating a vault whose id already exists is rejected too.

## Design

DDD-lite hexagonal layering. Dependencies point inward: `infrastructure → application → domain`, and `domain` depends on nothing.

```
dev.thgrcarvalho.fileshare
├── domain/                          ← types, contracts, validation
│   ├── Vault                        (AGGREGATE root: id + files + soft-delete lifecycle)
│   ├── VaultId / FileName / Bytes   (value objects)
│   ├── StoredFile                   (package-private: name + ciphertext + deleted flag)
│   ├── Cipher                       (port: encrypt / decrypt)
│   ├── VaultRepository              (port)
│   └── FileShareException           (sealed: UnknownVault / VaultAlreadyExists / UnknownFile /
│                                             FileAlreadyDeleted / FileNotDeleted)
├── application/
│   └── FileShareService             (use cases: create a vault, run operations, load → mutate → save)
└── infrastructure/
    ├── InMemoryVaultRepository      (Map-backed adapter)
    └── XorCipher                    (Cipher adapter — see the note below)
```

### The aggregate owns the files and their lifecycle

`Vault` is the **aggregate root**: it has an identity (`VaultId`) and owns its files. A file is a `StoredFile` — a name, its **ciphertext**, and a `deleted` flag — and the only way to change the set is through the Vault's own methods, so the cross-file invariant (one entry per name) can never be broken from outside. `StoredFile` is package-private and never escapes the aggregate; callers see file **names** (`listFiles`, `search`) and decrypted **bytes** (`read`), never the stored ciphertext.

### Encryption at rest, behind a port

Encryption is a `Cipher` **port** in the domain: `encrypt(Bytes) → Bytes`, `decrypt(Bytes) → Bytes`. `Vault.save` runs the plaintext through `encrypt` and stores only the result; `Vault.read` and content-search run the stored bytes back through `decrypt`. So the plaintext exists only transiently — as a method argument on the way in, and a return value on the way out — and what the repository persists is always ciphertext. The cipher is passed in as a collaborator, so storage and encryption are both swappable without touching the domain.

> **On the shipped cipher.** `XorCipher` is a **demonstration** cipher (a repeating-key XOR), chosen to honour the zero-dependency rule while still exercising the encryption boundary end to end. It is **not** cryptographically secure and is not meant for real data — it stands in for a real algorithm. Because `Cipher` is a port, dropping in a real adapter (e.g. AES-GCM via the JDK's `javax.crypto`) is a one-class change and is noted under Deferred.

### Soft delete and restore — distinct from save

`delete` doesn't drop the file; it flips it to a **deleted** state, so it disappears from `listFiles`/`search`/`read` but can be recovered. `restore` brings a deleted file back **with the exact content it had** when deleted. That is what makes `restore` meaningful and different from `save`: `save` always writes an active file with *new* content (creating it, overwriting an active one, or replacing a deleted one), whereas `restore` recovers the *original* content you'd otherwise have lost. (Once you `save` over a deleted name, that old content is gone — there's nothing left to restore.)

### Search by name and by content

`search` looks at active files only and matches a query **case-insensitively** against the file name **or** the decrypted content — so content search is what forces the decrypt-through-the-port path, the same boundary `read` uses. Filenames are case-**sensitive** for identity (`Report.txt` and `report.txt` are two files), but search is case-insensitive for convenience.

### Errors as API

The five domain failures share a **sealed** `FileShareException` base and carry typed accessors (`name()` / `id()`): `UnknownVaultException`, `VaultAlreadyExistsException`, `UnknownFileException`, `FileAlreadyDeletedException`, `FileNotDeletedException`. A caller can catch the category or switch it exhaustively. (A blank vault id or file name, or a null payload, is rejected earlier at value-object construction with `IllegalArgumentException` / `NullPointerException` — argument errors, kept separate from these domain-state failures.)

## Deferred (intentionally out of scope)

A real cipher (AES-GCM with proper key management and per-file IVs — the point here is the *boundary*, not the algorithm), concurrent access (the in-memory repository is thread-safe, but the service's load → mutate → save isn't serialized per vault — there was no concurrency requirement in this exercise, unlike the box office), a persistent repository adapter, searching inside deleted ("trash") files, per-vault quotas or size limits, file versioning/history, and any notion of users or sharing permissions.

## Build & test

```bash
./gradlew test
```

Requires JDK 21.

## Source

OOAD Challenges Round 2 — item 2, from [diegopacheco.github.io/tech-resources/java-resources.html](https://diegopacheco.github.io/tech-resources/java-resources.html).
