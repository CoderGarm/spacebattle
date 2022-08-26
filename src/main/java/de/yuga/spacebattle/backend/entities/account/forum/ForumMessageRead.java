package de.yuga.spacebattle.backend.entities.account.forum;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "forumMessageRead")
@AttributeOverride(name = "id", column = @Column(name = "idForumMessageRead"))
public class ForumMessageRead extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idForum")
    private Forum forum;

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idForumThread")
    private ForumThread forumThread;

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idForumMessage")
    private ForumMessage forumMessage;

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idUser")
    private User user;

    public ForumMessageRead() {
    }

    public ForumMessageRead(@Nonnull final Forum forum,
                            @Nonnull final ForumThread forumThread,
                            @Nonnull final ForumMessage forumMessage,
                            @Nonnull final User user) {
        Preconditions.checkNotNull(forum, "forum shouldn't be null!");
        Preconditions.checkNotNull(forumThread, "forumThread shouldn't be null!");
        Preconditions.checkNotNull(forumMessage, "forumMessage shouldn't be null!");
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        this.forum = forum;
        this.forumThread = forumThread;
        this.forumMessage = forumMessage;
        this.user = user;
    }

    @Nonnull
    public Forum getForum() {
        return forum;
    }

    @Nonnull
    public ForumThread getForumThread() {
        return forumThread;
    }

    @Nonnull
    public ForumMessage getForumMessage() {
        return forumMessage;
    }

    @Nonnull
    public User getUser() {
        return user;
    }
}
