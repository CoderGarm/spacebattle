package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.forum.ForumMessageRead;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface ForumMessageReadRepository extends PagingAndSortingRepository<ForumMessageRead, Integer> {

    @Query("SELECT CASE WHEN (COUNT(r) > 0) THEN TRUE ELSE FALSE END FROM ForumMessageRead r WHERE r.forumThread.id = :idForumThread AND r.user.id = :idUser AND r.isRead = false")
    boolean hasThreadUnread(@Param("idForumThread") final int idForumThread,
                            @Param("idUser") final int idUser);

    @Query("SELECT CASE WHEN (COUNT(r) > 0) THEN TRUE ELSE FALSE END FROM ForumMessageRead r WHERE r.forum.id = :idForum AND r.user.id = :idUser AND r.isRead = false")
    boolean hasForumUnread(@Param("idForum") final int idForum, @Param("idUser") final int idUser);

    @Query("SELECT CASE WHEN (COUNT(r) > 0) THEN TRUE ELSE FALSE END FROM ForumMessageRead r WHERE r.user.id = :idUser AND r.isRead = false " +
            "AND ((r.forum.alliance IS NULL OR r.forum.alliance = :alliance) OR ((r.forum.role IS NULL AND r.forum.alliance IS NULL) OR r.forum.role IN (:userRoles)))")
    boolean hasUserUnread(@Param("idUser") final int idUser, @Param("userRoles") final Set<EWebUserRole> userRoles, @Param("alliance") final Alliance alliance);

    @Nullable
    @Query("SELECT r.forumMessage.id FROM ForumMessageRead r WHERE r.forumThread.id = :idThread AND r.user.id = :idUser AND r.isRead = false")
    List<Integer> findUnreadMessages(final int idThread, final int idUser);

    @Nullable
    @Query("SELECT r FROM ForumMessageRead r WHERE r.user.id = :idUser AND r.isRead = false AND (r.forum.id = :idForum OR r.forumThread.id = :idForumThread OR r.forumMessage.id = :idForumMessage)")
    List<ForumMessageRead> getReads(@Nullable final Integer idForum, @Nullable final Integer idForumThread, @Nullable final Integer idForumMessage, final int idUser);

    @Nullable
    @Query("SELECT MIN(r.forumMessage.id) FROM ForumMessageRead r WHERE r.user.id = :idUser AND r.isRead = false AND r.forumThread.id = :idForumThread")
    Integer findFirstUnreadMessageId(final int idForumThread, final int idUser);
}
