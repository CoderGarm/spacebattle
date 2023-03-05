package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.ETechLevel;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
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

    public HasNamedTechLevel() {
    }

    public HasNamedTechLevel(@Nonnull final NamedTechLevel baseModule, @Nonnull final String technicalTypeName) {
        Preconditions.checkNotNull(baseModule, "baseModule must not be empty");
        Preconditions.checkNotNull(technicalTypeName, "technicalTypeName must not be empty");

        this.namedTechLevel = baseModule;
        this.technicalTypeName = technicalTypeName;
    }

    @Nonnull
    public String getTechnicalTypeName() {
        return technicalTypeName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamedTechLevel)) return false;

        NamedTechLevel module = (NamedTechLevel) o;
        return id == module.id;
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }
}
