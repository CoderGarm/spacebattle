package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public class HasNamedTechLevel extends AbstractEntityKey {

    @Nonnull
    @NotNull
    private String technicalTypeName;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idNamedTechLevel")
    private NamedTechLevel namedTechLevel;

    private int unlockedThroughLevel;

    public HasNamedTechLevel() {
    }

    public HasNamedTechLevel(@Nonnull final NamedTechLevel baseModule,
                             final int unlockedThroughLevel,
                             @Nonnull final String technicalTypeName) {
        Preconditions.checkNotNull(baseModule, "baseModule must not be empty");
        Preconditions.checkNotNull(technicalTypeName, "technicalTypeName must not be empty");

        this.namedTechLevel = baseModule;
        this.technicalTypeName = technicalTypeName;
        this.unlockedThroughLevel = unlockedThroughLevel;
    }

    @Nonnull
    public String getTechnicalTypeName() {
        return technicalTypeName;
    }

    @Nonnull
    public NamedTechLevel getNamedTechLevel() {
        return namedTechLevel;
    }

    @Nonnull
    public String getName(@Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        return namedTechLevel.getName(languageCode);
    }

    @Nonnull
    public String getDescription(@Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        return namedTechLevel.getDescription(languageCode);
    }

    @Nonnull
    public ETechLevel getTechLevel() {
        return namedTechLevel.getTechLevel();
    }

    public int getUnlockedThroughLevel() {
        return unlockedThroughLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HasNamedTechLevel)) return false;

        HasNamedTechLevel module = (HasNamedTechLevel) o;
        return id == module.id;
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }
}
