package de.yuga.spacebattle.misc.fandom;

import com.google.common.base.Preconditions;
import io.github.fastily.jwiki.core.Wiki;
import okhttp3.HttpUrl;

import javax.annotation.Nonnull;
import java.util.List;

public enum EWikiConfig {

    DE("de", "https://honor-harrington.fandom.com/de/api.php"),
    EN("en", "https://honorverse.fandom.com/api.php");

    @Nonnull
    private final String language;

    @Nonnull
    private final String url;

    EWikiConfig(@Nonnull final String language, @Nonnull final String url) {
        this.language = Preconditions.checkNotNull(language, "language must not be empty");
        this.url = Preconditions.checkNotNull(url, "url must not be empty");
    }

    @Nonnull
    public String getLanguage() {
        return language;
    }

    @Nonnull
    public String getUrl() {
        return url;
    }

    @Nonnull
    public Wiki getWiki() {
        return new Wiki.Builder()
                .withApiEndpoint(HttpUrl.get(url))
                .build();
    }

    @Nonnull
    public static List<EWikiConfig> get() {
        return List.of(EWikiConfig.values());
    }
}
