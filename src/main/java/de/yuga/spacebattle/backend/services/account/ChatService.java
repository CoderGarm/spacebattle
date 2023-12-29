package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.chat.MessageThread;
import de.yuga.spacebattle.backend.entities.account.chat.UserMessage;
import de.yuga.spacebattle.backend.repositories.account.MessageThreadRepository;
import de.yuga.spacebattle.backend.repositories.account.UserMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ChatService {

    @Nonnull
    private final MessageThreadRepository messageThreadRepository;

    @Nonnull
    private final UserMessageRepository userMessageRepository;

    @Nonnull
    private final NonPlayerCharacterService nonPlayerCharacterService;

    @Autowired
    public ChatService(@Nonnull final MessageThreadRepository messageThreadRepository,
                       @Nonnull final UserMessageRepository userMessageRepository,
                       @Nonnull final NonPlayerCharacterService nonPlayerCharacterService) {
        this.messageThreadRepository = Preconditions.checkNotNull(messageThreadRepository, "userMessageRepository should not be null");
        this.userMessageRepository = Preconditions.checkNotNull(userMessageRepository, "userMessageRepository shouldn't be null!");
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
    }

    /**
     * Returns all received messages of the current logged in user and marks them as read.
     *
     * @return empty if no user is logged in or no messages were received.
     */
    @Nullable
    public MessageThread findMessagesBetween(@Nonnull final Owner user1, @Nonnull final Owner user2) {
        Preconditions.checkNotNull(user1, "user1 shouldn't be null!");
        Preconditions.checkNotNull(user2, "user2 shouldn't be null!");

        return messageThreadRepository.findMessagesBetween(user1.getId(), user2.getId());
    }

    /**
     * Returns all received messages between two users.
     *
     * @return empty if no user is logged in or no messages were received.
     */
    @Nullable
    public MessageThread findMessagesBetween(final int idUser1, final int idUser2) {
        return messageThreadRepository.findMessagesBetween(idUser1, idUser2);
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

        MessageThread messageThread = messageThreadRepository.findByIdWithMessages(idUserMessage);
        if (messageThread != null) {
            final UserMessage userMessage = new UserMessage(messageThread, sender, chatMessage);
            messageThread.addMessage(userMessage);
            return save(messageThread);
        }
        return null;
    }

    @Nonnull
    public MessageThread createChatMessage(final Owner sender, final User receiver, @Nonnull final String message) {
        Preconditions.checkNotNull(sender, "sender shouldn't be null!");
        Preconditions.checkNotNull(receiver, "receiver shouldn't be null!");
        Preconditions.checkNotNull(message, "message shouldn't be null!");

        final MessageThread messagesBetween = findMessagesBetween(sender, receiver);
        final MessageThread anywayUsed = Objects.requireNonNullElseGet(messagesBetween, () -> new MessageThread(sender, receiver));
        final UserMessage userMessage = new UserMessage(anywayUsed, sender, message);
        anywayUsed.addMessage(userMessage);
        return save(anywayUsed);
    }

    /**
     * Marks a given user message as read if the user is the receiver of the message.
     *
     * @param idUserMessage the id of the message
     * @param idUser        the is of the receiver
     */
    public void markMessageReadIfForUser(final int idUserMessage, final int idUser) {
        userMessageRepository.getByIdIfUserIsReceiver(idUserMessage, idUser)
                .ifPresent(msg -> {
                    msg.setReceivedAt();
                    userMessageRepository.save(msg);
                });
    }

    /**
     * Returns if the thread has unread messages.
     *
     * @param idReceiver      the reader
     * @param idMessageThread the thread
     * @return <code>true</code> if the thread has unread messages, <code>false</code> otherwise
     */
    public boolean hasUnreadMessages(final int idReceiver, final int idMessageThread) {
        return userMessageRepository.hasUnreadMessages(idReceiver, idMessageThread);
    }

    /**
     * Returns if the user has unread messages.
     *
     * @param idUser the user who asks
     * @return <code>true</code> if the user has unread messages, <code>false</code> otherwise
     */
    public boolean hasUserUnreadMessages(final int idUser) {
        return userMessageRepository.hasUserUnreadMessages(idUser);
    }


    public void deleteForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        // fixme delete chats

        final Set<UserMessage> allForAuthors = Objects.requireNonNullElse(userMessageRepository.findAllForAuthor(user), new HashSet<>());
        userMessageRepository.deleteAll(allForAuthors);

        final Set<MessageThread> threadsForAuthors = Objects.requireNonNullElse(messageThreadRepository.findAllForAuthor(user), new HashSet<>());
        messageThreadRepository.deleteAll(threadsForAuthors);
    }
}
