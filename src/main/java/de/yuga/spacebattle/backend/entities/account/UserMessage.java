package de.yuga.spacebattle.backend.entities.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "userMessage")
@AttributeOverride(name = "id", column = @Column(name = "idUserMessage"))
public class UserMessage extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idMessageThread")
    private MessageThread messageThread;

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idUserSender")
    private User sender;

    @Nonnull
    @NotNull
    private String message;

    @Nonnull
    @NotNull
    private LocalDateTime sentAt;

    @Nullable
    private LocalDateTime receivedAt;

    public UserMessage() {
    }

    public UserMessage(@Nonnull final MessageThread messageThread, @Nonnull final User sender, @Nonnull final String message) {
        Preconditions.checkNotNull(messageThread, "messageThread shouldn't be null!");
        Preconditions.checkNotNull(sender, "sender shouldn't be null!");
        Preconditions.checkNotNull(message, "message shouldn't be null!");

        this.messageThread = messageThread;
        this.sender = sender;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }

    @Nonnull
    public MessageThread getMessageThread() {
        return messageThread;
    }

    @Nonnull
    public User getSender() {
        return sender;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }

    @Nullable
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    @Nonnull
    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public boolean isUnRead() {
        return this.receivedAt == null;
    }

    public void setReceivedAt() {
        if (this.receivedAt == null) {
            this.receivedAt = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserMessage)) return false;

        UserMessage that = (UserMessage) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }
}
