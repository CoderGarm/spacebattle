package de.yuga.spacebattle.rest.dto.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ETutorialCategory;
import de.yuga.spacebattle.backend.enums.EWikiCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class Article {

    @JsonProperty
    @Schema(required = true, description = "The id")
    private final int idArticle;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The title")
    private final String title;


    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The language code")
    private final String langCode;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The category")
    private final EWikiCategory wikiCategory;

    @Nullable
    @JsonProperty
    @Schema(description = "The category")
    private final ETutorialCategory tutorialCategory;

    public Article(@Nonnull final de.yuga.spacebattle.backend.entities.wiki.Article article) {
        Preconditions.checkNotNull(article, "article must not be empty");

        this.idArticle = article.getId();
        this.title = article.getTitle();
        this.langCode = article.getLangCode();
        this.wikiCategory = article.getWikiCategory();
        this.tutorialCategory = article.getTutorialCategory();
    }
}
