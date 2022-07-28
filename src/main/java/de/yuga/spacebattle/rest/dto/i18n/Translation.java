package de.yuga.spacebattle.rest.dto.i18n;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.i18n.Translatable;
import de.yuga.spacebattle.backend.enums.ETranslatableType;
import de.yuga.spacebattle.backend.enums.ETranslationTarget;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Size;

@Schema(description = ".")
public class Translation {

    @Schema(required = true, description = "The id of the translation where this dto belongs to.")
    private int idTranslatable;

    @Nonnull
    @JsonProperty
    @Schema(description = "For what kind of item the translation is for.")
    private ETranslationTarget translationTarget;

    @Nonnull
    @JsonProperty
    @Schema(description = "Which kind of test will be translated by this item.")
    private ETranslatableType translatableType;

    @Nullable
    @JsonProperty
    @Schema(description = "The database id of the parent item.")
    private Integer idParent;

    @Nonnull
    @Size(min = 2, max = 2)
    @Schema(required = true, description = "The language code of this localization.")
    private String languageCode;

    @Nonnull
    @Schema(required = true, description = "The localized string.")
    private String translation;

    public Translation() {
    }

    public Translation(@Nonnull final Translatable translatable,
                       @Nonnull final de.yuga.spacebattle.backend.entities.i18n.Translation translation) {
        Preconditions.checkNotNull(translatable, "translatable must not be empty");
        Preconditions.checkNotNull(translation, "translation must not be empty");

        this.idTranslatable = translatable.getId();
        this.translationTarget = translatable.getTranslationTarget();
        this.translatableType = translatable.getTranslatableType();
        this.idParent = translatable.getIdParent();
        this.languageCode = translation.getLanguageCode();
        this.translation = translation.getTranslation();
    }

    public int getIdTranslatable() {
        return idTranslatable;
    }

    public void setIdTranslatable(final int idTranslatable) {
        this.idTranslatable = idTranslatable;
    }

    @Nonnull
    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(@Nonnull final String languageCode) {
        this.languageCode = languageCode;
    }

    @Nonnull
    public String getTranslation() {
        return translation;
    }

    public void setTranslation(@Nonnull final String translation) {
        this.translation = translation;
    }
}
