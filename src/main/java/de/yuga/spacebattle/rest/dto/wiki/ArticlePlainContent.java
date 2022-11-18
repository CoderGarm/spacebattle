package de.yuga.spacebattle.rest.dto.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class ArticlePlainContent {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The current revision")
    private ArticleRevision revision;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The current revision")
    private String content;

    public ArticlePlainContent(@Nonnull final de.yuga.spacebattle.backend.entities.wiki.ArticleRevision articleRevision) {
        Preconditions.checkNotNull(articleRevision, "articleRevision must not be empty");

        this.revision = new ArticleRevision(articleRevision);
        this.content = articleRevision.asPlainString();
    }

    @Nonnull
    public ArticleRevision getRevision() {
        return revision;
    }

    @Nonnull
    public String getContent() {
        return content;
    }
}
