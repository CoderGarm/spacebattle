package de.yuga.spacebattle.rest.dto.account.forum;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.Player;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

@Schema(description = "The entry of a forum's thread.")
public class ForumMessage {

    @JsonProperty
    @Schema(required = true, description = "The id of the message.")
    private int idForumMessage;

    @JsonProperty
    @Schema(required = true, description = "The id of the message's parent thread.")
    private int idForumThread;

    @JsonProperty
    @Schema(required = true, description = "The id of the forum.")
    private int idForum;

    @JsonProperty
    @Schema(required = true, description = "The author.")
    private Player author;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user's profile pic.")
    private String profilePic;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The message itself.")
    private String message;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The creation timestamp.")
    private LocalDateTime sentAt;

    public ForumMessage() {
    }

    public ForumMessage(@Nonnull final de.yuga.spacebattle.backend.entities.account.forum.ForumMessage message) {
        Preconditions.checkNotNull(message, "message shouldn't be null!");

        this.idForumMessage = message.getId();
        this.idForumThread = message.getForumThread().getId();
        this.idForum = message.getForumThread().getForum().getId();
        this.profilePic = message.getAuthor().getUserSetting().getProfilePic();
        this.author = new Player(message.getAuthor());
        this.message = message.getMessage();
        this.sentAt = message.getSentAt();
    }

    public int getIdForumMessage() {
        return idForumMessage;
    }

    @Nonnull
    public String getMessage() {
        return message;
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
