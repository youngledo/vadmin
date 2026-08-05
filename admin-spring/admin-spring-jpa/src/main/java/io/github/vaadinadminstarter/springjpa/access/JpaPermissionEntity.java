package io.github.vaadinadminstarter.springjpa.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "permissions")
public class JpaPermissionEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "system_managed", nullable = false)
    private boolean systemManaged;

    protected JpaPermissionEntity() { }

    public JpaPermissionEntity(UUID id, String code, boolean systemManaged) {
        this.id = id;
        this.code = code;
        this.systemManaged = systemManaged;
    }

    public UUID id() {
        return id;
    }

    public String code() {
        return code;
    }

    public boolean systemManaged() {
        return systemManaged;
    }
}
