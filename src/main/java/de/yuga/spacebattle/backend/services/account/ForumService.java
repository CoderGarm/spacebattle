package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.forum.IdToId;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessageRead;
import de.yuga.spacebattle.backend.entities.account.forum.ForumThread;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import de.yuga.spacebattle.backend.repositories.account.ForumMessageReadRepository;
import de.yuga.spacebattle.backend.repositories.account.ForumMessageRepository;
import de.yuga.spacebattle.backend.repositories.account.ForumRepository;
import de.yuga.spacebattle.backend.repositories.account.ForumThreadRepository;
import de.yuga.spacebattle.backend.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ForumService {

    @Nonnull
    private final ForumRepository forumRepository;

    @Nonnull
    private final ForumThreadRepository forumThreadRepository;

    @Nonnull
    private final ForumMessageRepository forumMessageRepository;

    @Nonnull
    private final ForumMessageReadRepository messageReadRepository;

    @Nonnull
    private final MailService mailService;

    @Nonnull
    private final UserService userService;

    @Autowired
    public ForumService(@Nonnull final ForumRepository forumRepository,
                        @Nonnull final ForumThreadRepository forumThreadRepository,
                        @Nonnull final ForumMessageRepository forumMessageRepository,
                        @Nonnull final ForumMessageReadRepository messageReadRepository,
                        @Nonnull final MailService mailService,
                        @Nonnull final UserService userService) {
        this.forumRepository = Preconditions.checkNotNull(forumRepository, "forumRepository shouldn't be null!");
        this.forumThreadRepository = Preconditions.checkNotNull(forumThreadRepository, "forumThreadRepository shouldn't be null!");
        this.forumMessageRepository = Preconditions.checkNotNull(forumMessageRepository, "forumMessageRepository shouldn't be null!");
        this.messageReadRepository = Preconditions.checkNotNull(messageReadRepository, "messageReadRepository shouldn't be null!");
        this.mailService = Preconditions.checkNotNull(mailService, "mailService must not be empty");
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
    }

    @Nonnull
    public List<Forum> saveAll(@Nonnull final Collection<Forum> toStore) {
        Preconditions.checkNotNull(toStore, "toStore shouldn't be null!");

        final Iterable<Forum> forums = forumRepository.saveAll(toStore);
        return StreamSupport.stream(forums.spliterator(), false).collect(Collectors.toList());
    }

    @Nonnull
    public List<Forum> findForumsAllowedForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        final Iterable<Forum> forums = forumRepository.findAll();
        final List<Forum> forumList = StreamSupport.stream(forums.spliterator(), false).collect(Collectors.toList());
        return forumList.stream().filter(f -> f.isUserAllowed(user)).collect(Collectors.toList());
    }


    @Nonnull
    public List<IdToId> findAllIdForumThreadForIdForums(@Nonnull final List<Integer> idForums) {
        Preconditions.checkNotNull(idForums, "idForums shouldn't be null!");

        return forumThreadRepository.findAllIdThreadForForums(idForums);
    }

    @Nonnull
    public ForumMessage createForumMessage(@Nonnull final ForumThread forumThread,
                                           @Nonnull final User author,
                                           @Nonnull final String message) {
        Preconditions.checkNotNull(forumThread, "messageThread shouldn't be null!");
        Preconditions.checkNotNull(author, "author shouldn't be null!");
        Preconditions.checkNotNull(message, "message shouldn't be null!");

        final ForumMessage saved = save(new ForumMessage(forumThread, author, message));
        forumThread.setLastChanged(saved.getSentAt());
        save(forumThread);
        return saved;
    }

    @Nullable
    public ForumThread findForumThread(final int idForumThread) {
        return forumThreadRepository.findById(idForumThread).orElse(null);
    }

    @Nonnull
    public List<ForumThread> findForumThreads(@Nonnull final List<Integer> idForumThreads) {
        Preconditions.checkNotNull(idForumThreads, "idForumThreads shouldn't be null!");

        final Iterable<ForumThread> allById = forumThreadRepository.findAllById(idForumThreads);
        return StreamSupport.stream(allById.spliterator(), false).collect(Collectors.toList());
    }

    @Nonnull
    public List<IdToId> getMessageIdsForThread(final int idForumThread) {
        return forumMessageRepository.findAllMessageIdsForThreadId(List.of(idForumThread));
    }

    @Nullable
    public Forum findForumById(final int idForum) {
        return forumRepository.findById(idForum).orElse(null);
    }

    @Nonnull
    public ForumThread save(final ForumThread forumThread) {
        return forumThreadRepository.save(forumThread);
    }

    @Nonnull
    public ForumMessage save(final ForumMessage forumMessage) {
        return forumMessageRepository.save(forumMessage);
    }

    @Nonnull
    public List<ForumMessage> findMessagesInForumThread(final int idForumThread, final int page, final int size) {
        return forumMessageRepository.findMessagesWithPaging(idForumThread, page, size);
    }

    public int findPageWithFirstUnreadMessageInThread(final int idUser, final int idForumThread, final int size) {
        final Integer firstUnreadMessageId = messageReadRepository.findFirstUnreadMessageId(idForumThread, idUser);
        int numberOfStackToFirstUnread;
        if (firstUnreadMessageId != null) {
            numberOfStackToFirstUnread = forumMessageRepository.getThreadSizeUpToMessageId(idForumThread, firstUnreadMessageId);
        } else {
            numberOfStackToFirstUnread = countMessagesInForumThread(idForumThread);
        }
        //noinspection UnnecessaryLocalVariable
        final int page = numberOfStackToFirstUnread / size;
        return page;
    }

    @Nonnull
    public List<Forum> findAll() {
        final Iterable<Forum> forums = forumRepository.findAll();
        return StreamSupport.stream(forums.spliterator(), false).collect(Collectors.toList());
    }

    public int countMessagesInForumThread(final int idForumThread) {
        return forumMessageRepository.countMessagesInThreadById(idForumThread);
    }

    @Nonnull
    public Forum getAllianceForumForUser(@Nonnull final Alliance alliance) {
        Preconditions.checkNotNull(alliance, "alliance shouldn't be null!");

        return forumRepository.getAllianceForumForUser(alliance);
    }

    public void save(@Nonnull final Forum forum) {
        Preconditions.checkNotNull(forum, "forum shouldn't be null!");

        forumRepository.save(forum);
    }

    public void delete(@Nonnull final Forum forum) {
        Preconditions.checkNotNull(forum, "forum shouldn't be null!");

        forumRepository.delete(forum);
    }

    /**
     * Returns if the specific thread contains unread messages.
     *
     * @param idForumThread the thread
     * @param idUser        the user
     * @return <code>false</code> if all messages of the thread were read, <code>true</code> otherwise
     */
    public boolean hasThreadUnread(final int idForumThread, final int idUser) {
        return messageReadRepository.hasThreadUnread(idForumThread, idUser);
    }

    /**
     * Returns if the specific forum contains unread messages.
     *
     * @param idForum the forum
     * @param idUser  the user
     * @return <code>false</code> if all messages of all threads of the forum were read, <code>true</code> otherwise
     */
    public boolean hasForumUnread(final int idForum, final int idUser) {
        return messageReadRepository.hasForumUnread(idForum, idUser);
    }

    /**
     * Returns if the specific user has unread forum messages.
     */
    public boolean hasUserUnread(final int idUser) {
        final EWebUserRole role = userService.findUserRole(idUser);
        final Alliance alliance = userService.findAlliance(idUser);
        final Set<EWebUserRole> userRoles = role != null ? role.getAllowedRoles() : Set.of();
        return messageReadRepository.hasUserUnread(idUser, userRoles, alliance);
    }

    public void markMessageRead(@Nullable final Integer idForum,
                                @Nullable final Integer idForumThread,
                                @Nullable final Integer idForumMessage,
                                final int idUser) {
        final List<ForumMessageRead> reads = Objects.requireNonNullElse(messageReadRepository.getReads(idForum, idForumThread, idForumMessage, idUser), new ArrayList<>());
        reads.forEach(r -> r.setRead(true));
        messageReadRepository.saveAll(reads);
    }

    @Nullable
    public ForumMessage findMessage(final int idForumMessage) {
        return forumMessageRepository.findById(idForumMessage).orElse(null);
    }

    public void sendRelease(@Nonnull final Set<String> recipients, int idForumThread) {
        Preconditions.checkNotNull(recipients, "recipients must not be empty");

        final ForumThread thread = forumThreadRepository.findById(idForumThread).orElse(null);
        if (thread == null) {
            return;
        }

        final String title = thread.getTitle();
        final String description = thread.getDescription();
        final List<ForumMessage> messages = Objects.requireNonNullElse(findMessagesInForumThread(idForumThread, 0, 1), new ArrayList<>());
        if (!messages.isEmpty()) {
            mailService.sendRelease(title, description, messages.get(0).getMessage(), recipients);
        }
    }

    @Nonnull
    public List<Integer> findUnreadMessages(final int idThread, final int idUser) {
        return Objects.requireNonNullElse(messageReadRepository.findUnreadMessages(idThread, idUser), new ArrayList<>());
    }
}
