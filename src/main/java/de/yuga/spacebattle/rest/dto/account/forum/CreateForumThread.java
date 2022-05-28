package de.yuga.spacebattle.rest.dto.account.forum;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = "A forum thread on creation.")
public class CreateForumThread {

    @JsonProperty
    @Schema(required = true, description = "The id of the parent's forum.")
    private int idForum;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The title of the thread.")
    private String title;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The description of the thread.")
    private String description;

    @Nullable
    @JsonProperty
    @Schema(description = "The id of the thread.")
    private String firstMessage;

    public CreateForumThread() {
    }

    public int getIdForum() {
        return idForum;
    }

    @Nonnull
    public String getTitle() {
        return title;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getFirstMessage() {
        return firstMessage;
    }
}
