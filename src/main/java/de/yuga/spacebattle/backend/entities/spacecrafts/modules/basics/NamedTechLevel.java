package de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics;

import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasName;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.ETranslationTarget;
import jakarta.persistence.*;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "namedTechLevel")
@AttributeOverride(name = "id", column = @Column(name = "idNamedTechLevel"))
public class NamedTechLevel extends HasName {

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ETranslationTarget translationTarget;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ETechLevel techLevel;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;

    protected NamedTechLevel() {
    }

    public NamedTechLevel(@Nonnull final String name,
                          @Nonnull final String description,
                          @Nonnull final Research unlockedThrough,
                          @Nonnull final ETechLevel techLevel,
                          @Nonnull final Class<?> clazz) {
        super(new Translation(Translation.DEFAULT_LANGUAGE, name), new Translation(Translation.DEFAULT_LANGUAGE, description), techLevel, clazz);

        this.translationTarget = ETranslationTarget.getByClazz(clazz);
        this.techLevel = techLevel;
        this.unlockedThrough = unlockedThrough;
    }

    @Nonnull
    public ETranslationTarget getTranslationTarget() {
        return translationTarget;
    }

    @Nonnull
    public ETechLevel getTechLevel() {
        return techLevel;
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
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
