package de.yuga.spacebattle.backend.entities.i18n;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.ETranslatableType;
import de.yuga.spacebattle.backend.enums.ETranslationTarget;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static de.yuga.spacebattle.backend.entities.i18n.Translation.DEFAULT_LANGUAGE;

/**
 * A translatable bundles a language code to a string in this language.
 */
@Entity
@Table(name = "translatable")
@AttributeOverride(name = "id", column = @Column(name = "idTranslatable"))
public class Translatable extends AbstractEntityKey {

    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    private ETranslationTarget translationTarget;

    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    private ETranslatableType translatableType;

    @NotNull
    private int idParent;

    /**
     * At least one translation with the language code <code>en</code> must be present as default.
     */
    @Nonnull
    @Size(min = 1)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "translationCollection",
            joinColumns = @JoinColumn(name = "idTranslatable"),
            inverseJoinColumns = @JoinColumn(name = "idTranslation"))
    private final List<Translation> translations = new ArrayList<>();

    public Translatable() {
    }

    public Translatable(@Nonnull final AbstractEntityKey parent, @Nonnull final ETranslationTarget translationTarget, @Nonnull final ETranslatableType translatableType) {
        Preconditions.checkNotNull(parent, "parent must not be empty");
        Preconditions.checkNotNull(translationTarget, "translationTarget must not be empty");
        Preconditions.checkNotNull(translatableType, "translatableType must not be empty");

        this.idParent = parent.getId();
        this.translationTarget = translationTarget;
        this.translatableType = translatableType;
    }

    @Nonnull
    public ETranslationTarget getTranslationTarget() {
        return translationTarget;
    }

    @Nonnull
    public ETranslatableType getTranslatableType() {
        return translatableType;
    }

    public int getIdParent() {
        return idParent;
    }

    public void add(@Nonnull final Translation translation) {
        Preconditions.checkNotNull(translation, "translation must not be empty");

        translations.add(translation);
    }

    @Nonnull
    public String getTranslation(@Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        final Translation translation = translations.stream()
                .filter(t -> t.getLanguageCode().equals(languageCode))
                .findFirst()
                .orElse(null);
        if (translation != null) {
            return translation.getTranslation();
        }
        return getTranslation(DEFAULT_LANGUAGE);
    }

    @Nonnull
    public List<Translation> getTranslations() {
        return translations;
    }

    public void updateOrCreate(@Nonnull final String languageCode, @Nonnull final String translationText) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(translationText, "translationText must not be empty");

        final Translation translation = getTranslations().stream().filter(tr -> tr.getLanguageCode().equals(languageCode)).findFirst().orElse(null);
        if (translation != null) {
            translation.setTranslation(translationText);
        } else {
            add(new Translation(languageCode, translationText));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Translatable that = (Translatable) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
