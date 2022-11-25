package de.yuga.spacebattle.rest.dto.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.AbstractId;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class ArticleRevision {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The author")
    private AbstractId author;

    @JsonProperty
    @Schema(required = true, description = "The version")
    private int version;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The article")
    private AbstractId article;

    public ArticleRevision(@Nonnull final de.yuga.spacebattle.backend.entities.wiki.ArticleRevision rev) {
        Preconditions.checkNotNull(rev, "rev must not be empty");

        this.author = new AbstractId(rev.getAuthor());
        this.version = rev.getVersion();
        this.article = new AbstractId(rev.getArticle());
    }
}
