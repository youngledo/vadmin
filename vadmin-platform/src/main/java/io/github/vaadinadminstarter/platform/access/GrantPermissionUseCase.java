package io.github.vaadinadminstarter.platform.access;

import io.github.vaadinadminstarter.contracts.auth.CurrentUser;

public interface GrantPermissionUseCase { void grant(CurrentUser actor, GrantPermissionCommand command); }
