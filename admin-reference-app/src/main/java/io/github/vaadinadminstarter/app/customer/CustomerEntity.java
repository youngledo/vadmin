package io.github.vaadinadminstarter.app.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
class CustomerEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerEntity() { }

    CustomerEntity(UUID id, String name, String email, Instant now) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.active = true;
        this.createdAt = now;
        this.updatedAt = now;
    }

    void update(String name, String email, boolean active, Instant now) {
        this.name = name;
        this.email = email;
        this.active = active;
        this.updatedAt = now;
    }

    Customer toCustomer() {
        return new Customer(id, name, email, active, createdAt);
    }
}
