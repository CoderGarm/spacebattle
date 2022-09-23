package de.yuga.spacebattle.backend.entities.account.forum;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.rest.dto.account.forum.CreateForumThread;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "ForumThread.findAllIdThreadForForums",
                query = "SELECT new de.yuga.spacebattle.backend.dto.forum.IdToId(t.forum.id, t.id) FROM ForumThread t WHERE t.forum.id IN (:idForums)"),
})
@Entity
@Table(name = "forumThread")
@AttributeOverride(name = "id", column = @Column(name = "idForumThread"))
public class ForumThread extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idForum")
    private Forum forum;

    @Nonnull
    @NotNull
    private String title;

    @Nonnull
    @NotNull
    private String description;

    @Nonnull
    @NotNull
    private LocalDateTime createdAt;

    @Nonnull
    @NotNull
    private LocalDateTime lastChanged;

    @Nonnull
    @NotNull
    @OneToMany
    @JoinColumn(name = "idForumThread")
    private final Set<ForumMessage> messages = new HashSet<>();

    public ForumThread() {
    }

    public ForumThread(@Nonnull final Forum forum,
                       @Nonnull final String title,
                       @Nonnull final String description) {
        Preconditions.checkNotNull(forum, "forum shouldn't be null!");
        Preconditions.checkNotNull(title, "title shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");

        this.forum = forum;
        this.title = title;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.lastChanged = createdAt;
    }

    public ForumThread(@Nonnull final Forum forum, @Nonnull final CreateForumThread createForumThread) {
        this(forum, createForumThread.getTitle(), createForumThread.getDescription());
    }

    @Nonnull
    public Forum getForum() {
        return forum;
    }

    @Nonnull
    public String getTitle() {
        return title;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Nonnull
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setLastChanged(@Nonnull final LocalDateTime lastChanged) {
        Preconditions.checkNotNull(lastChanged, "lastChanged must not be empty");

        this.lastChanged = lastChanged;
    }

    @Nonnull
    public LocalDateTime getLastChanged() {
        return lastChanged;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof ForumThread)) return false;

        final ForumThread that = (ForumThread) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
