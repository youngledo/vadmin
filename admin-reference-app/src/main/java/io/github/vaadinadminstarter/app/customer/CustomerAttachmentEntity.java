package io.github.vaadinadminstarter.app.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_attachments")
class CustomerAttachmentEntity {
    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "stored_file_id", nullable = false, unique = true)
    private UUID storedFileId;

    @Column(nullable = false)
    private String filename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CustomerAttachmentEntity() { }

    CustomerAttachmentEntity(UUID id, UUID customerId, UUID storedFileId, String filename, String contentType,
                             long size, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.storedFileId = storedFileId;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.createdAt = createdAt;
    }

    CustomerAttachment toAttachment() {
        return new CustomerAttachment(id, customerId, storedFileId, filename, contentType, size, createdAt);
    }
}
