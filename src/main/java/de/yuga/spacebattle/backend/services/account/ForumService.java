package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.forum.IdToId;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import de.yuga.spacebattle.backend.entities.account.forum.ForumThread;
import de.yuga.spacebattle.backend.repositories.account.ForumMessageRepository;
import de.yuga.spacebattle.backend.repositories.account.ForumRepository;
import de.yuga.spacebattle.backend.repositories.account.ForumThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
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

    @Autowired
    public ForumService(@Nonnull final ForumRepository forumRepository,
                        @Nonnull final ForumThreadRepository forumThreadRepository,
                        @Nonnull final ForumMessageRepository forumMessageRepository) {
        Preconditions.checkNotNull(forumRepository, "forumRepository shouldn't be null!");
        Preconditions.checkNotNull(forumThreadRepository, "forumThreadRepository shouldn't be null!");
        Preconditions.checkNotNull(forumMessageRepository, "forumMessageRepository shouldn't be null!");

        this.forumRepository = forumRepository;
        this.forumThreadRepository = forumThreadRepository;
        this.forumMessageRepository = forumMessageRepository;
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
        return forumMessageRepository.findReportsWithUserWithPaging(idForumThread, page, size);
    }

    @Nonnull
    public List<Forum> findAll() {
        final Iterable<Forum> forums = forumRepository.findAll();
        return StreamSupport.stream(forums.spliterator(), false).collect(Collectors.toList());
    }

}
