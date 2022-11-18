package de.yuga.spacebattle.rest.dto.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.enums.EWikiCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;

@Schema(description = ".")
public class ArticleEdit {

    @JsonProperty
    @Schema(required = true, description = "The id of the article to edit")
    private int idArticle;

    @Nullable
    @JsonProperty
    @Schema(description = "The title")
    private String title;

    @Nullable
    @JsonProperty
    @Schema(description = "The category")
    private EWikiCategory wikiCategory;

    @Nullable
    @JsonProperty
    @Schema(description = "The content")
    private String content;

    public ArticleEdit() {
    }

    public int getIdArticle() {
        return idArticle;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public EWikiCategory getWikiCategory() {
        return wikiCategory;
    }

    @Nullable
    public String getContent() {
        return content;
    }
}
