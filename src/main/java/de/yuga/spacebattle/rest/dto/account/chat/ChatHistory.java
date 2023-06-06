package de.yuga.spacebattle.rest.dto.account.chat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.chat.MessageThread;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Schema(description = ".")
public class ChatHistory {

    @Nullable
    @Schema(description = "The id of this chat history")
    private Integer idChatHistory;

    @Nullable
    @Schema(required = true, description = "One of the involved users.")
    private UserJson userOne;

    @Nullable
    @Schema(required = true, description = "The other involved user.")
    private UserJson userTwo;

    @Nonnull
    @Schema(required = true, description = "The amount of messages.")
    private final List<ChatMessage> messages = new ArrayList<>();

    public ChatHistory() {
    }

    public ChatHistory(@Nonnull final MessageThread messageThread) {
        Preconditions.checkNotNull(messageThread, "messageThread shouldn't be null!");

        this.idChatHistory = messageThread.getId();
        this.userOne = new UserJson(messageThread.getUserOne());
        this.userTwo = new UserJson(messageThread.getUserTwo());
        if (messageThread.hasMessagesInitialized()) {
            messageThread.getMessages().stream().map(ChatMessage::new).forEach(this.messages::add);
        }
    }

    @Nullable
    public Integer getIdChatHistory() {
        return idChatHistory;
    }

    @Nonnull
    public UserJson getUserOne() {
        return Objects.requireNonNull(userOne);
    }

    @Nonnull
    public UserJson getUserTwo() {
        return Objects.requireNonNull(userTwo);
    }

    @Nonnull
    public List<ChatMessage> getMessages() {
        return messages;
    }
}
