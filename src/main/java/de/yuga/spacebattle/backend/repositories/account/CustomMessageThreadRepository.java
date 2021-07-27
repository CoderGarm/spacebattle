package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.MessageThread;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CustomMessageThreadRepository {

    /**
     * Searches all messages between two users.
     *
     * @param idUser1 the first user
     * @param idUser2 the second user
     * @return all messages
     */
    @Nullable
    MessageThread findMessagesBetween(final int idUser1, final int idUser2);

    /**
     * Searches all users which has exchanged messages with the given user.
     *
     * @param idUser the user which is part of the message exchange
     * @return the list of users which has exchanged messages with
     */
    @Nonnull
    List<MessageThread> findThreadsWithUser(final int idUser);

    @Nullable
    MessageThread findByIdWithMessages(final int idMessageThread);
}
