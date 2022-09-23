package de.yuga.spacebattle.rest.dto.account.chat;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.UserMessage;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;

@Schema(description = ".")
public class ChatMessage {

    @Nullable
    @Schema(description = "The id of this message.")
    private Integer idUserMessage;

    @Nullable
    @Schema(required = true, description = "The message's sender.")
    private UserJson sender;

    @Nullable
    @Schema(required = true, description = "The message itself") // todo escape
    private String message;

    @Nullable
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(required = true, description = "The timestamp on which the message was sent.")
    private LocalDateTime sentAt;

    @Nullable
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "The timestamp on which the message was read.")
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

    @Nullable
    public UserJson getSender() {
        return sender;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public LocalDateTime getSentAt() {
        return sentAt;
    }

    @Nullable
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
}
