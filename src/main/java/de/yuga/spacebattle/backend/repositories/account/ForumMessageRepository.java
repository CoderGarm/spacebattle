package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public interface ForumMessageRepository extends PagingAndSortingRepository<ForumMessage, Integer>, CustomForumMessageRepository {

    @Query("SELECT COUNT(r) FROM ForumMessage r WHERE r.forumThread.id = :idForumThread AND r.id <= :idForumMessage")
    int getThreadSizeUpToMessageId(final int idForumThread, final int idForumMessage);

    @Nullable
    @Query("SELECT m FROM ForumMessage m WHERE m.author = :user")
    Set<ForumMessage> findAllForAuthor(@Nonnull final User user);
}
