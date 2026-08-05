package io.github.vaadinadminstarter.app.administration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import java.lang.reflect.Method;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserAdministrationServiceTest {
    @Test
    void exposesABulkEnableDisableCommand() {
        assertThat(findBulkSetEnabledMethod()).isNotNull();
    }

    private Method findBulkSetEnabledMethod() {
        try {
            return UserAdministrationService.class.getMethod("setEnabled", CurrentUser.class, Set.class, boolean.class);
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }
}
