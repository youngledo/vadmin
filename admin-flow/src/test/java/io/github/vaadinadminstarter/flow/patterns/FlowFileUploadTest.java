package io.github.vaadinadminstarter.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.file.FileStorage;
import io.github.vaadinadminstarter.contracts.file.StoredFile;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FlowFileUploadTest {
    @Test
    void requiresPermissionBeforePassingUploadToStorage() {
        var permission = PermissionCode.of("customer:attachment:upload");
        var user = new CurrentUser(UUID.randomUUID(), "operator", Set.of(permission), 0);
        var storage = new RecordingStorage();
        var upload = new FlowFileUpload(new PermitAllAuthorization(), storage);

        var stored = upload.store(user, permission, "contract.txt", "text/plain",
                new ByteArrayInputStream("content".getBytes()));

        assertThat(stored.filename()).isEqualTo("contract.txt");
        assertThat(storage.called).isTrue();
    }

    private static final class PermitAllAuthorization implements AuthorizationService {
        @Override public boolean hasPermission(CurrentUser user, PermissionCode permission) { return true; }
        @Override public void requirePermission(CurrentUser user, PermissionCode permission) { }
    }

    private static final class RecordingStorage implements FileStorage {
        private boolean called;
        @Override public StoredFile store(String filename, String contentType, InputStream content) {
            called = true;
            return new StoredFile(UUID.randomUUID(), filename, contentType, 7);
        }
        @Override public InputStream open(UUID id) { throw new UnsupportedOperationException(); }
        @Override public void delete(UUID id) { throw new UnsupportedOperationException(); }
    }
}
