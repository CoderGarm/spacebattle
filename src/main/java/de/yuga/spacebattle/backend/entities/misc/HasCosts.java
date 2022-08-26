package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.entities.i18n.Translatable;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResourceDemand;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.ETranslatableType;
import de.yuga.spacebattle.backend.enums.ETranslationTarget;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Simply the entity key.
 */
@MappedSuperclass
public class HasCosts extends AbstractEntityKey {

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

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ETechLevel techLevel;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private ResourceDeposit costs;

    public HasCosts() {
    }

    @PostPersist
    public void setParentId() {
        name.setIdParent(getId());
        description.setIdParent(getId());
    }

    public HasCosts(@Nonnull final Translation translatableName,
                    @Nonnull final Translation translatableDescription,
                    @Nonnull final ETechLevel techLevel,
                    @Nonnull final Class<?> clazz) {
        Preconditions.checkNotNull(translatableName, "translatableName must not be empty");
        Preconditions.checkNotNull(translatableDescription, "translatableDescription must not be empty");
        Preconditions.checkArgument(translatableName.getLanguageCode().equals(Translation.DEFAULT_LANGUAGE), "translatableName: common language must be english");
        Preconditions.checkArgument(translatableDescription.getLanguageCode().equals("en"), "translatableDescription: common language must be english");
        Preconditions.checkNotNull(techLevel, "techLevel shouldn't be null!");
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");

        this.name = new Translatable(ETranslationTarget.getByClazz(clazz), ETranslatableType.NAME);
        this.name.add(translatableName);
        this.description = new Translatable(ETranslationTarget.getByClazz(clazz), ETranslatableType.DESCRIPTION);
        this.description.add(translatableDescription);
        this.techLevel = techLevel;
        this.costs = ResourceDepositInitializerCalculator.initializeResourceDeposit(techLevel, EResourceDemand.getByClazz(this.getClass()));
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
    public ETechLevel getTechLevel() {
        return techLevel;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }
}
