package io.github.youngledo.vadmin.contracts.audit;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class AuditMetadataRedactor {
    private static final Set<String> FORBIDDEN = Set.of("password", "secret", "token", "authorization", "cookie", "sql", "stack");
    public Map<String, String> redact(Map<String, String> metadata) {
        return metadata.entrySet().stream().filter(entry -> FORBIDDEN.stream()
                .noneMatch(word -> entry.getKey().toLowerCase(Locale.ROOT).contains(word)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
