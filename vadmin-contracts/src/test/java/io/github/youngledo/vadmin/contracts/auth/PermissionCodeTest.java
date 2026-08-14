package io.github.youngledo.vadmin.contracts.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PermissionCodeTest {

    @Test
    void acceptsThreeSegmentPermissionCode() {
        assertThat(PermissionCode.of("system:user:read").value()).isEqualTo("system:user:read");
    }

    @Test
    void rejectsMissingActionSegment() {
        assertThatThrownBy(() -> PermissionCode.of("system:user"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsUppercaseAndInvalidCharacters() {
        assertThatThrownBy(() -> PermissionCode.of("system:User:read"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
