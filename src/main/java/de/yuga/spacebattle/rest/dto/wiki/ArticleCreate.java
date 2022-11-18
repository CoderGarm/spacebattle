package de.yuga.spacebattle.rest.dto.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.enums.EWikiCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class ArticleCreate {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The title")
    private String title;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The category")
    private EWikiCategory wikiCategory;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The content")
    private String content;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The language this article is for")
    private String langCode;

    public ArticleCreate() {
    }

    @Nonnull
    public String getTitle() {
        return title;
    }

    @Nonnull
    public EWikiCategory getWikiCategory() {
        return wikiCategory;
    }

    @Nonnull
    public String getContent() {
        return content;
    }

    @Nonnull
    public String getLangCode() {
        return langCode;
    }
}
