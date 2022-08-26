package de.yuga.spacebattle.backend.entities.account.forum;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NamedQueries({
        @NamedQuery(name = "ForumMessage.findAllMessageIdsForThread",
                query = "SELECT new de.yuga.spacebattle.backend.dto.forum.IdToId(t.forumThread.id, t.id) FROM ForumMessage t WHERE t.forumThread.id IN (:idForumThreads)"),
        @NamedQuery(name = "ForumMessage.findPagedMessages",
                query = "SELECT r FROM ForumMessage r WHERE r.forumThread.id = :idForumThread ORDER BY r.id ASC"),
        @NamedQuery(name = "ForumMessage.countMessagesInThreadById",
                query = "SELECT COUNT(t) FROM ForumMessage t WHERE t.forumThread.id = :idForumThread")
})
@Entity
@Table(name = "forumMessage")
@AttributeOverride(name = "id", column = @Column(name = "idForumMessage"))
public class ForumMessage extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idForumThread")
    private ForumThread forumThread;

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idUserAuthor")
    private User author;

    @Nonnull
    @NotNull
    @Column(columnDefinition = "varchar(10000)")
    private String message;

    @Nonnull
    @NotNull
    private LocalDateTime sentAt;

    public ForumMessage() {
    }

    public ForumMessage(@Nonnull final ForumThread forumThread,
                        @Nonnull final User author,
                        @Nonnull final String message) {
        Preconditions.checkNotNull(forumThread, "messageThread shouldn't be null!");
        Preconditions.checkNotNull(author, "author shouldn't be null!");
        Preconditions.checkNotNull(message, "message shouldn't be null!");

        this.forumThread = forumThread;
        this.author = author;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }

    @Nonnull
    public ForumThread getForumThread() {
        return forumThread;
    }

    @Nonnull
    public User getAuthor() {
        return author;
    }

    public void setMessage(@Nonnull final String message) {
        Preconditions.checkNotNull(message, "message shouldn't be null!");

        this.message = message;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }

    @Nonnull
    public LocalDateTime getSentAt() {
        return sentAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof ForumMessage)) return false;

        final ForumMessage that = (ForumMessage) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
