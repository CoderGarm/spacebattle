package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.MessageThread;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.UserMessage;
import de.yuga.spacebattle.backend.repositories.account.MessageThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MessageThreadService {

    @Nonnull
    private final MessageThreadRepository messageThreadRepository;

    @Autowired
    public MessageThreadService(@Nonnull final MessageThreadRepository messageThreadRepository) {
        Preconditions.checkNotNull(messageThreadRepository, "userMessageRepository should not be null");

        this.messageThreadRepository = messageThreadRepository;
    }

    /**
     * Returns all received messages of the current logged in user and marks them as read.
     *
     * @return empty if no user is logged in or no messages were received.
     */
    @Nullable
    public MessageThread findMessagesBetween(@Nonnull final User user1, @Nonnull final User user2) {
        Preconditions.checkNotNull(user1, "user1 shouldn't be null!");
        Preconditions.checkNotNull(user2, "user2 shouldn't be null!");

        final MessageThread messageThread = messageThreadRepository.findMessagesBetween(user1.getId(), user2.getId());
        if (messageThread == null) {
            return null;
        }
        final Set<UserMessage> unreadMessages = messageThread.getMessages().stream().filter(UserMessage::isUnRead).collect(Collectors.toSet());
        unreadMessages.forEach(UserMessage::setReceivedAt);
        save(messageThread);
        return messageThread;
    }

    /**
     * Searches all users which had a chat with the reference user.<br>
     * The result does NOT contain the reference user.
     *
     * @param user the user who had chats
     * @return all users who were the other side of the chat
     */
    @Nonnull
    public List<MessageThread> findThreadsWithUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return messageThreadRepository.findThreadsWithUser(user.getId());
    }

    /**
     * Returns all received messages of the current logged in user and marks them as read.
     *
     * @return empty if no user is logged in or no messages were received.
     */
    @Nullable
    public MessageThread findMessagesBetween(final int idUser1, final int idUser2) {
        final MessageThread messageThread = messageThreadRepository.findMessagesBetween(idUser1, idUser2);
        if (messageThread == null) {
            return null;
        }
        final Set<UserMessage> unreadMessages = messageThread.getMessages().stream().filter(UserMessage::isUnRead).collect(Collectors.toSet());
        unreadMessages.forEach(UserMessage::setReceivedAt);
        save(messageThread);
        return messageThread;
    }

    /**
     * Searches all users which had a chat with the reference user.<br>
     * The result does NOT contain the reference user.
     *
     * @param idUser the user who had chats
     * @return all users who were the other side of the chat
     */
    @Nonnull
    public List<MessageThread> findThreadsWithUser(final int idUser) {
        return messageThreadRepository.findThreadsWithUser(idUser);
    }

    @Nonnull
    public MessageThread save(@Nonnull final MessageThread entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return messageThreadRepository.save(entity);
    }

    @Nullable
    public MessageThread sendChatMessage(final int idUserMessage, final User sender, @Nonnull final String chatMessage) {
        Preconditions.checkNotNull(sender, "sender shouldn't be null!");
        Preconditions.checkNotNull(chatMessage, "chatMessage shouldn't be null!");

        // todo escape message
        MessageThread messageThread = messageThreadRepository.findByIdWithMessages(idUserMessage);
        if (messageThread != null) {
            final UserMessage userMessage = new UserMessage(messageThread, sender, chatMessage);
            messageThread.addMessage(userMessage);
            return save(messageThread);
        }
        return null;
    }

    @Nonnull
    public MessageThread createChatMessage(final User sender, final User receiver, @Nonnull final String message) {
        Preconditions.checkNotNull(sender, "sender shouldn't be null!");
        Preconditions.checkNotNull(receiver, "receiver shouldn't be null!");
        Preconditions.checkNotNull(message, "message shouldn't be null!");

        // todo escape message
        final MessageThread anywayUsed;
        final MessageThread messagesBetween = findMessagesBetween(sender, receiver);
        if (messagesBetween != null) {
            anywayUsed = messagesBetween;
        } else {
            anywayUsed = new MessageThread(sender, receiver);
        }
        final UserMessage userMessage = new UserMessage(anywayUsed, sender, message);
        anywayUsed.addMessage(userMessage);
        return save(anywayUsed);
    }
}
