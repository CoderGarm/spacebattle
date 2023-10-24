package de.yuga.spacebattle.rest.dto.account.forum;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.List;

@Schema(description = "The entry of a forum's thread.")
public class ForumMessageContainer {

    @JsonProperty
    @Schema(required = true, description = "The page of the message.")
    private int page;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The messages.")
    private List<ForumMessage> messages;


    public ForumMessageContainer(final int page, @Nonnull final List<ForumMessage> messages) {
        Preconditions.checkNotNull(messages, "messages must not be empty");

        this.page = page;
        this.messages = messages;
    }
}
