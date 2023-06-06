package de.yuga.spacebattle.backend.entities.account.chat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NamedQueries({
        @NamedQuery(name = "MessageThread.findThreadsWithUser",
                query = "SELECT DISTINCT mt FROM MessageThread mt WHERE mt.userTwo.id = :idUser OR mt.userOne.id = :idUser"),
        @NamedQuery(name = "MessageThread.findMessagesBetween",
                query = "SELECT DISTINCT mt FROM MessageThread mt LEFT JOIN FETCH mt.messages WHERE (mt.userTwo.id = :idUser1 OR mt.userOne.id = :idUser1) AND (mt.userTwo.id = :idUser2 OR mt.userOne.id = :idUser2) "),
        @NamedQuery(name = "MessageThread.findByIdWithMessages",
                query = "SELECT DISTINCT mt FROM MessageThread mt LEFT JOIN FETCH mt.messages WHERE mt.id=:idMessageThread"),
})
@Entity
@Table(name = "messageThread", uniqueConstraints = @UniqueConstraint(name = "messageThread_UC", columnNames = {"idUserOne", "idUserTwo"}))
@AttributeOverride(name = "id", column = @Column(name = "idMessageThread"))
public class MessageThread extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idUserOne")
    private User userOne;

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idUserTwo")
    private User userTwo;

    @Nonnull
    @NotNull
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idMessageThread")
    private final Set<UserMessage> messages = new HashSet<>();

    public MessageThread() {
    }

    public MessageThread(@Nonnull final User userOne, @Nonnull final User userTwo) {
        Preconditions.checkNotNull(userOne, "userOne shouldn't be null!");
        Preconditions.checkNotNull(userTwo, "userTwo shouldn't be null!");

        this.userOne = userOne;
        this.userTwo = userTwo;
    }

    @Nonnull
    public User getUserOne() {
        return userOne;
    }

    @Nonnull
    public User getUserTwo() {
        return userTwo;
    }

    /**
     * Checks if the messages are loaded.
     *
     * @return <code>true</code> if the messages for this thread are initialized, <code>false</code> otherwise
     */
    public boolean hasMessagesInitialized() {
        return Persistence.getPersistenceUtil().isLoaded(this, "messages");
    }

    @Nonnull
    public List<UserMessage> getMessages() {
        return messages.stream().sorted(Comparator.comparing(UserMessage::getSentAt)).collect(Collectors.toList());
    }

    public void addMessage(@Nonnull final UserMessage userMessage) {
        Preconditions.checkNotNull(userMessage, "userMessage shouldn't be null!");

        messages.add(userMessage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageThread)) return false;

        MessageThread thread = (MessageThread) o;

        if (!userOne.equals(thread.userOne)) return false;
        return userTwo.equals(thread.userTwo);
    }

    @Override
    public int hashCode() {
        int result = userOne.hashCode();
        result = 31 * result + userTwo.hashCode();
        return result;
    }
}
