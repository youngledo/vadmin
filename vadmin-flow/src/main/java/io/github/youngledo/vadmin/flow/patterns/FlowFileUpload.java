package io.github.youngledo.vadmin.flow.patterns;

import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.contracts.file.FileStorage;
import io.github.youngledo.vadmin.contracts.file.StoredFile;
import java.io.InputStream;
import java.util.Objects;

/** UI-facing upload boundary that always authorizes before content reaches storage. */
public final class FlowFileUpload {
    private final AuthorizationService authorization;
    private final FileStorage storage;

    public FlowFileUpload(AuthorizationService authorization, FileStorage storage) {
        this.authorization = Objects.requireNonNull(authorization);
        this.storage = Objects.requireNonNull(storage);
    }

    public StoredFile store(CurrentUser user, PermissionCode permission, String filename, String contentType,
                            InputStream content) {
        authorization.requirePermission(user, permission);
        return storage.store(filename, contentType, content);
    }
}
