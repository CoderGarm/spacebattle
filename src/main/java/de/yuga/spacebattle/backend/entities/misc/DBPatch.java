package de.yuga.spacebattle.backend.entities.misc;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "dbPatch")
@AttributeOverride(name = "id", column = @Column(name = "idDBPatch"))
public class DBPatch extends AbstractEntityKey {

    @Nonnull
    @NotNull
    private String version;

    @Nonnull
    @NotNull
    private String description;

    @Nonnull
    @NotNull
    private LocalDateTime createdAt;

    public DBPatch() {
    }

    @Nonnull
    public String getVersion() {
        return version;
    }

    @Nonnull
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }
}
