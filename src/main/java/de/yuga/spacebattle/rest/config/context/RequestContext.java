package de.yuga.spacebattle.rest.config.context;

import de.yuga.spacebattle.backend.entities.i18n.Translation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

import static de.yuga.spacebattle.backend.entities.i18n.Translation.DEFAULT_LANGUAGE;

public class RequestContext {


    @Nonnull
    private String token = "token not sent";

    @Nonnull
    private String acceptedLanguage = DEFAULT_LANGUAGE;

    @Nullable
    private Locale locale;


    public void setToken(@Nullable final String token) {
        if (!StringUtils.isEmpty(token)) {
            this.token = token;
        }
    }

    public void setLanguage(@Nullable final String language) {
        if (!StringUtils.isEmpty(language)) {
            this.acceptedLanguage = language;
        }
    }

    @Nonnull
    public String getToken() {
        return token;
    }

    @Nonnull
    public String getAcceptedLanguage() {
        if (locale == null) {
            final List<Locale.LanguageRange> languageRanges = Locale.LanguageRange.parse(acceptedLanguage);
            locale = Locale.lookup(languageRanges, Translation.LOCALES);
        }
        return locale.getLanguage();
    }
}
