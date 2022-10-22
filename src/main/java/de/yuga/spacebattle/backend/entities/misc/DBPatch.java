package de.yuga.spacebattle.backend.entities.misc;

import javax.annotation.Nonnull;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
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
