package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.i18n.Translatable;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.ETranslatableType;
import de.yuga.spacebattle.backend.enums.ETranslationTarget;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import jakarta.persistence.*;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public class HasName extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "idTranslatableName")
    private Translatable name;

    @Nonnull
    @NotNull
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "idTranslatableDescription")
    private Translatable description;

    public HasName() {
    }

    @PostPersist
    public void setParentId() {
        name.setIdParent(getId());
        description.setIdParent(getId());
    }

    public HasName(@Nonnull final Translation translatableName,
                   @Nonnull final Translation translatableDescription,
                   @Nonnull final ETechLevel techLevel,
                   @Nonnull final Class<?> clazz) {
        Preconditions.checkNotNull(translatableName, "translatableName must not be empty");
        Preconditions.checkNotNull(translatableDescription, "translatableDescription must not be empty");
        Preconditions.checkArgument(translatableName.getLanguageCode().equals(Translation.DEFAULT_LANGUAGE), "translatableName: common language must be english");
        Preconditions.checkArgument(translatableDescription.getLanguageCode().equals(Translation.DEFAULT_LANGUAGE), "translatableDescription: common language must be english");
        Preconditions.checkNotNull(techLevel, "techLevel shouldn't be null!");
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");

        final ETranslationTarget translationTarget = ETranslationTarget.getByClazz(clazz);
        this.name = new Translatable(translationTarget, ETranslatableType.NAME);
        this.name.add(translatableName);
        this.description = new Translatable(translationTarget, ETranslatableType.DESCRIPTION);
        this.description.add(translatableDescription);
    }

    @Nonnull
    public String getName(@Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        return name.getTranslation(languageCode);
    }

    @Nonnull
    public String getDescription(@Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        return description.getTranslation(languageCode);
    }

    @Nonnull
    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public Translatable getName() {
        return name;
    }

    @Nonnull
    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public Translatable getDescription() {
        return description;
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
