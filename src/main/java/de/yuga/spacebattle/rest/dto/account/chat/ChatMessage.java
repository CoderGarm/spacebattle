package de.yuga.spacebattle.rest.dto.account.chat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.UserMessage;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Objects;

public class ChatMessage {

    @Nullable
    @ApiModelProperty("The id of this message.")
    private Integer idUserMessage;

    @Nullable
    @ApiModelProperty(required = true, value = "The message's sender.")
    private UserJson sender;

    @Nullable
    @ApiModelProperty(required = true, value = "The message itself") // todo escape
    private String message;

    @Nullable
    @ApiModelProperty(required = true, value = "The timestamp on which the message was sent.")
    private LocalDateTime sentAt;

    @Nullable
    @ApiModelProperty("The timestamp on which the message was read.")
    private LocalDateTime receivedAt;

    public ChatMessage() {
    }

    public ChatMessage(@Nonnull final UserMessage userMessage) {
        Preconditions.checkNotNull(userMessage, "userMessage shouldn't be null!");

        this.idUserMessage = userMessage.getId();
        this.sender = new UserJson(userMessage.getSender());
        this.message = userMessage.getMessage();
        this.sentAt = userMessage.getSentAt();
        this.receivedAt = userMessage.getReceivedAt();
    }

    @Nullable
    public Integer getIdUserMessage() {
        return idUserMessage;
    }

    @Nonnull
    public UserJson getSender() {
        return Objects.requireNonNull(sender);
    }

    @Nonnull
    public String getMessage() {
        return Objects.requireNonNull(message);
    }

    @Nonnull
    public LocalDateTime getSentAt() {
        return Objects.requireNonNull(sentAt);
    }

    @Nullable
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
}
