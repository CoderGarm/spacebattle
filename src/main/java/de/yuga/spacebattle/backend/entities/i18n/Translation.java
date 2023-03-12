package de.yuga.spacebattle.backend.entities.i18n;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.util.Locale.*;

@Entity
@Table(name = "translation")
@AttributeOverride(name = "id", column = @Column(name = "idTranslation"))
public class Translation extends AbstractEntityKey {

    /**
     * The default language for all translatable names and descriptions.
     */
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String SECOND_LANGUAGE = "de";

    /**
     * Best matching possibilities.
     */
    @Nonnull
    public static final List<Locale> LOCALES = Arrays.asList(
            ENGLISH,
            FRENCH,
            GERMAN,
            ITALIAN
    );

    @Nonnull
    @Size(min = 2, max = 3)
    private String languageCode;

    @Nonnull
    @Column(length = 400)
    private String translation;

    @ManyToOne
    @JoinColumn(name = "idTranslatable")
    private Translatable translatable;

    public Translation() {
    }

    public Translation(@Nonnull final String languageCode, @Nonnull final String translation) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(translation, "translation must not be empty");

        this.languageCode = languageCode;
        this.translation = translation;
    }

    @Nonnull
    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(@Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.languageCode = languageCode;
    }

    @Nonnull
    public String getTranslation() {
        return translation;
    }

    public void setTranslation(@Nonnull final String translation) {
        Preconditions.checkNotNull(translation, "translation must not be empty");

        this.translation = translation;
    }

    void setTranslatable(final Translatable translatable) {
        this.translatable = translatable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Translation that = (Translation) o;

        return new EqualsBuilder().append(languageCode, that.languageCode).append(translation, that.translation).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(languageCode).append(translation).toHashCode();
    }
}
