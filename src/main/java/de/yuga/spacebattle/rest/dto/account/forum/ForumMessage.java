package de.yuga.spacebattle.rest.dto.account.forum;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

@Schema(description = "The entry of a forum's thread.")
public class ForumMessage extends AbstractEntityKey {

    @JsonProperty
    @Schema(required = true, description = "The id of the message.")
    private final int idForumMessage;

    @JsonProperty
    @Schema(required = true, description = "The id of the message's parent thread.")
    private final int idForumThread;

    @JsonProperty
    @Schema(required = true, description = "The id of the forum.")
    private final int idForum;

    @JsonProperty
    @Schema(required = true, description = "The id of the author.")
    private final int idAuthor;

    @JsonProperty
    @Schema(required = true, description = "The username of the author.")
    private final String author;

    @JsonProperty
    @Schema(required = true, description = "The message itself.")
    private final String message;

    @JsonProperty
    @Schema(required = true, description = "The creation timestamp.")
    private final LocalDateTime sentAt;

    public ForumMessage(@Nonnull final de.yuga.spacebattle.backend.entities.account.forum.ForumMessage message) {
        Preconditions.checkNotNull(message, "message shouldn't be null!");

        this.idForumMessage = message.getId();
        this.idForumThread = message.getForumThread().getId();
        this.idForum = message.getForumThread().getForum().getId();
        this.idAuthor = message.getAuthor().getId();
        this.author = message.getAuthor().getUsername();
        this.message = message.getMessage();
        this.sentAt = message.getSentAt();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof ForumMessage)) return false;

        final ForumMessage that = (ForumMessage) o;

        return new EqualsBuilder().append(idForumMessage, that.idForumMessage).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(idForumMessage).toHashCode();
    }
}
