package dev.thgrcarvalho.fileshare.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record SearchHit(FileName name, Set<MatchField> matchedOn, Optional<String> snippet) {

    public SearchHit {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(matchedOn, "matchedOn");
        Objects.requireNonNull(snippet, "snippet");
        if (matchedOn.isEmpty()) {
            throw new IllegalArgumentException("a search hit must match on at least one field");
        }
        if (snippet.isPresent() != matchedOn.contains(MatchField.CONTENT)) {
            throw new IllegalArgumentException("snippet must be present exactly when the content matched");
        }
        matchedOn = Set.copyOf(matchedOn);
    }

    public boolean matchedName() {
        return matchedOn.contains(MatchField.NAME);
    }

    public boolean matchedContent() {
        return matchedOn.contains(MatchField.CONTENT);
    }
}
