package io.github.vaadinadminstarter.app.customer;

import io.github.vaadinadminstarter.contracts.audit.AuditEvent;
import io.github.vaadinadminstarter.contracts.audit.AuditOutcome;
import io.github.vaadinadminstarter.contracts.audit.AuditSink;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.contracts.error.ErrorCode;
import io.github.vaadinadminstarter.contracts.file.FileStorage;
import io.github.vaadinadminstarter.contracts.file.StoredFile;
import io.github.vaadinadminstarter.contracts.navigation.PagedQuery;
import io.github.vaadinadminstarter.contracts.navigation.PagedResult;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class CustomerService {
    private static final PermissionCode READ = PermissionCode.of("customer:customer:read");
    private static final PermissionCode CREATE = PermissionCode.of("customer:customer:create");
    private static final PermissionCode UPDATE = PermissionCode.of("customer:customer:update");
    private static final PermissionCode DELETE = PermissionCode.of("customer:customer:delete");
    private static final PermissionCode ATTACHMENT_UPLOAD = PermissionCode.of("customer:attachment:upload");

    private final EntityManager entityManager;
    private final AuthorizationService authorization;
    private final AuditSink auditSink;
    private final FileStorage fileStorage;

    public CustomerService(EntityManager entityManager, AuthorizationService authorization, AuditSink auditSink,
                           FileStorage fileStorage) {
        this.entityManager = entityManager;
        this.authorization = authorization;
        this.auditSink = auditSink;
        this.fileStorage = fileStorage;
    }

    @Transactional(readOnly = true)
    public List<Customer> list(CurrentUser actor, String filter) {
        authorization.requirePermission(actor, READ);
        var value = filter == null ? "" : filter.strip();
        return entityManager.createQuery("""
                        select customer from CustomerEntity customer
                        where lower(customer.name) like lower(:filter)
                           or lower(customer.email) like lower(:filter)
                        order by customer.name, customer.email
                        """, CustomerEntity.class)
                .setParameter("filter", "%" + value + "%")
                .setMaxResults(100)
                .getResultList()
                .stream()
                .map(CustomerEntity::toCustomer)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResult<Customer> page(CurrentUser actor, PagedQuery query) {
        authorization.requirePermission(actor, READ);
        var filter = query.filters().getOrDefault("q", "").strip();
        var value = "%" + filter + "%";
        var total = entityManager.createQuery("""
                        select count(customer) from CustomerEntity customer
                        where lower(customer.name) like lower(:filter)
                           or lower(customer.email) like lower(:filter)
                        """, Long.class)
                .setParameter("filter", value)
                .getSingleResult();
        var items = entityManager.createQuery("""
                        select customer from CustomerEntity customer
                        where lower(customer.name) like lower(:filter)
                           or lower(customer.email) like lower(:filter)
                        order by customer.name, customer.email
                        """, CustomerEntity.class)
                .setParameter("filter", value)
                .setFirstResult(Math.multiplyExact(query.page(), query.pageSize()))
                .setMaxResults(query.pageSize())
                .getResultList()
                .stream()
                .map(CustomerEntity::toCustomer)
                .toList();
        return new PagedResult<>(items, total);
    }

    @Transactional
    public Customer create(CurrentUser actor, String name, String email) {
        authorization.requirePermission(actor, CREATE);
        var customer = new CustomerEntity(UUID.randomUUID(), required("name", name), required("email", email), Instant.now());
        entityManager.persist(customer);
        audit(actor, "customer:customer:create", customer.toCustomer().id());
        return customer.toCustomer();
    }

    @Transactional
    public Customer update(CurrentUser actor, UUID id, String name, String email, boolean active) {
        authorization.requirePermission(actor, UPDATE);
        var customer = entityManager.find(CustomerEntity.class, id);
        if (customer == null) {
            throw new BusinessFailure(ErrorCode.RESOURCE_NOT_FOUND, "customer.not-found", Map.of("id", "unknown"));
        }
        customer.update(required("name", name), required("email", email), active, Instant.now());
        audit(actor, "customer:customer:update", id);
        return customer.toCustomer();
    }

    @Transactional
    public void delete(CurrentUser actor, UUID id) {
        authorization.requirePermission(actor, DELETE);
        var customer = entityManager.find(CustomerEntity.class, id);
        if (customer == null) {
            throw new BusinessFailure(ErrorCode.RESOURCE_NOT_FOUND, "customer.not-found", Map.of("id", "unknown"));
        }
        var storedFileIds = entityManager.createQuery("select attachment from CustomerAttachmentEntity attachment "
                        + "where attachment.customerId = :customerId", CustomerAttachmentEntity.class)
                .setParameter("customerId", id)
                .getResultList()
                .stream()
                .map(attachment -> attachment.toAttachment().storedFileId())
                .toList();
        entityManager.remove(customer);
        audit(actor, "customer:customer:delete", id);
        deleteAfterCommit(storedFileIds);
    }

    @Transactional
    public CustomerAttachment attach(CurrentUser actor, UUID customerId, StoredFile storedFile) {
        try {
            authorization.requirePermission(actor, ATTACHMENT_UPLOAD);
            requireCustomer(customerId);
            var attachment = new CustomerAttachmentEntity(UUID.randomUUID(), customerId, storedFile.id(),
                    storedFile.filename(), storedFile.contentType(), storedFile.size(), Instant.now());
            entityManager.persist(attachment);
            audit(actor, "customer:attachment:upload", customerId);
            deleteOnRollback(storedFile.id());
            return attachment.toAttachment();
        } catch (RuntimeException exception) {
            fileStorage.delete(storedFile.id());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<CustomerAttachment> attachments(CurrentUser actor, UUID customerId) {
        authorization.requirePermission(actor, READ);
        requireCustomer(customerId);
        return entityManager.createQuery("select attachment from CustomerAttachmentEntity attachment "
                        + "where attachment.customerId = :customerId order by attachment.createdAt", CustomerAttachmentEntity.class)
                .setParameter("customerId", customerId)
                .getResultList()
                .stream()
                .map(CustomerAttachmentEntity::toAttachment)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerAttachmentDownload openAttachment(CurrentUser actor, UUID attachmentId) {
        authorization.requirePermission(actor, READ);
        var attachment = entityManager.find(CustomerAttachmentEntity.class, attachmentId);
        if (attachment == null) {
            throw new BusinessFailure(ErrorCode.RESOURCE_NOT_FOUND, "attachment.not-found", Map.of("id", "unknown"));
        }
        var metadata = attachment.toAttachment();
        return new CustomerAttachmentDownload(metadata, fileStorage.open(metadata.storedFileId()));
    }

    private String required(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessFailure(ErrorCode.VALIDATION_FAILED, "validation.failed", Map.of(field, "required"));
        }
        return value.strip();
    }

    private void audit(CurrentUser actor, String action, UUID customerId) {
        auditSink.append(new AuditEvent(actor.userId(), action, "customer", customerId.toString(), AuditOutcome.SUCCESS,
                Instant.now(), null, Map.of()));
    }

    private CustomerEntity requireCustomer(UUID customerId) {
        var customer = entityManager.find(CustomerEntity.class, customerId);
        if (customer == null) {
            throw new BusinessFailure(ErrorCode.RESOURCE_NOT_FOUND, "customer.not-found", Map.of("id", "unknown"));
        }
        return customer;
    }

    private void deleteOnRollback(UUID storedFileId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    fileStorage.delete(storedFileId);
                }
            }
        });
    }

    private void deleteAfterCommit(List<UUID> storedFileIds) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storedFileIds.forEach(fileStorage::delete);
            }
        });
    }
}
