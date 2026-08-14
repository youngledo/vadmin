package io.github.youngledo.vadmin.platform.access;

import io.github.youngledo.vadmin.contracts.auth.CurrentUser;

public interface GrantPermissionUseCase { void grant(CurrentUser actor, GrantPermissionCommand command); }
