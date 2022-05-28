package de.yuga.spacebattle.rest.dto.account.forum;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = "A forum thread message on creation.")
public class CreateForumThreadMessage {

    @JsonProperty
    @Schema(required = true, description = "The id of the parent's forum thread.")
    private int idForumThread;

    @Nonnull
    @JsonProperty
    @Schema(description = "The message.")
    private String message;

    public CreateForumThreadMessage() {
    }

    public int getIdForumThread() {
        return idForumThread;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }
}
