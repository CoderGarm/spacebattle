package de.yuga.spacebattle.backend.entities.i18n;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.ETranslatableType;
import de.yuga.spacebattle.backend.enums.ETranslationTarget;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

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

    private int idParent;

    /**
     * At least one translation with the language code <code>en</code> must be present as default.
     */
    @Nonnull
    @Size(min = 1)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "translatable")
    private final Set<Translation> translations = new HashSet<>();

    public Translatable() {
    }

    public Translatable(@Nonnull final ETranslationTarget translationTarget, @Nonnull final ETranslatableType translatableType) {
        Preconditions.checkNotNull(translationTarget, "translationTarget must not be empty");
        Preconditions.checkNotNull(translatableType, "translatableType must not be empty");

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

    public void setIdParent(final int idParent) {
        this.idParent = idParent;
    }

    public int getIdParent() {
        return idParent;
    }

    public void add(@Nonnull final Translation translation) {
        Preconditions.checkNotNull(translation, "translation must not be empty");

        translation.setTranslatable(this);
        translations.add(translation);
    }

    @Nonnull
    public String getTranslation(@Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        if (translations.isEmpty()) {
            throw new NotifyWebUserException("No translations present. This must be checked!");
        }
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
    public Set<Translation> getTranslations() {
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
