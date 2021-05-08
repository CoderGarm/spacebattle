package de.yuga.spacebattle.backend.entities.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity for a UserMessage.
 */
@Entity
@Table(name = "userMessage")
@AttributeOverride(name = "id", column = @Column(name = "idUserMessage"))
public class UserMessage extends AbstractEntityKey {

    @Nonnull
    @NotNull(message = "idUserSender should not be null")
    @ManyToOne(optional = false)
    @JoinColumn(name = "idUserSender")
    private User userSender;

    @Nonnull
    @NotNull(message = "idUserReceiver should not be null")
    @ManyToOne(optional = false)
    @JoinColumn(name = "idUserReceiver")
    private User userReceiver;

    @Nonnull
    @NotNull(message = "subject should not be null")
    private String subject;

    @Nonnull
    @Nullable
    private String message;

    @Nonnull
    @NotNull(message = "sentAt should not be null")
    private LocalDateTime sentAt;

    @Nullable
    private LocalDateTime receivedAt;

    /**
     * Default constructor.
     */
    public UserMessage() {
    }

    /**
     * Create a new Message with the current user as sender.
     * @param userSender {@link User}
     */
    public UserMessage(User userSender) {
        Preconditions.checkNotNull(userSender);

        this.userSender = userSender;
    }

    @Nonnull
    public User getUserSender() {
        return userSender;
    }

    @Nonnull
    public User getUserReceiver() {
        return userReceiver;
    }

    @Nonnull
    public String getSubject() {
        return subject;
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

    public void setUserReceiver(@Nonnull User userReceiver) {
        Preconditions.checkNotNull(userReceiver);

        this.userReceiver = userReceiver;
    }

    public void setSubject(@Nonnull String subject) {
        Preconditions.checkNotNull(subject);

        this.subject = subject;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSentAt() {
        this.sentAt = LocalDateTime.now();
    }

    public void setReceivedAt() {
        if (this.receivedAt == null) {
            this.receivedAt = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserMessage that = (UserMessage) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
