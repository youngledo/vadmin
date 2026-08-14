package io.github.vaadinadminstarter.springjpa.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class JpaRoleEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    protected JpaRoleEntity() { }

    public JpaRoleEntity(UUID id, String code) {
        this.id = id;
        this.code = code;
    }

    public UUID id() {
        return id;
    }

    public String code() {
        return code;
    }
}
