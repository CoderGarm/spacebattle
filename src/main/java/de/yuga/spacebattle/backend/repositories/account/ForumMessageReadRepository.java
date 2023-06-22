package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.forum.ForumMessageRead;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ForumMessageReadRepository extends PagingAndSortingRepository<ForumMessageRead, Integer>, JpaRepository<ForumMessageRead, Integer>, CustomForumMessageReadRepository {

    /**
     * Returns if the specific message was read or not.
     *
     * @param idForumThread  the thread
     * @param idForumMessage the message
     * @param idUser         the user
     * @return <code>false</code> if it was read, <code>true</code> otherwise
     */
    @Query("SELECT CASE WHEN (COUNT(r) = 0) THEN TRUE ELSE FALSE END FROM ForumMessageRead r " +
            "WHERE r.forumThread.id = :idForumThread " +
            "AND r.forumMessage.id = :idForumMessage " +
            "AND r.user.id = :idUser")
    boolean isMessageUnread(@Param("idForumThread") final int idForumThread,
                            @Param("idForumMessage") final int idForumMessage,
                            @Param("idUser") final int idUser);

    /**
     * Returns if the specific thread contains unread messages.
     *
     * @param idForumThread the thread
     * @param idUser        the user
     * @return <code>false</code> if all messages of the thread were read, <code>true</code> otherwise
     */
    @Query("SELECT CASE WHEN (COUNT(r) <> (SELECT COUNT(m) FROM ForumMessage m WHERE m.forumThread.id = :idForumThread)) THEN TRUE ELSE FALSE END FROM ForumMessageRead r " +
            "WHERE r.forumThread.id = :idForumThread " +
            "AND r.user.id = :idUser")
    boolean hasThreadUnread(@Param("idForumThread") final int idForumThread,
                            @Param("idUser") final int idUser);

    /**
     * Returns if the specific forum contains unread messages.
     *
     * @param idForum the forum
     * @param idUser  the user
     * @return <code>false</code> if all messages of all threads of the forum were read, <code>true</code> otherwise
     */
    @Query("SELECT CASE WHEN (COUNT(r) <> (SELECT COUNT(m) FROM ForumMessage m WHERE m.forumThread.forum.id = :idForum)) THEN TRUE ELSE FALSE END " +
            "FROM ForumMessageRead r " +
            "WHERE r.forum.id = :idForum " +
            "AND r.user.id = :idUser")
    boolean hasForumUnread(@Param("idForum") final int idForum,
                           @Param("idUser") final int idUser);

    /**
     * Returns if the specific user has unread forum messages.
     *
     * @param idUser the user
     * @return <code>false</code> if all messages of all forums were read, <code>true</code> otherwise
     */
    @Query("SELECT CASE WHEN (COUNT(r) <> (" +
            "   SELECT COUNT(m) " +
            "   FROM Forum f " +
            "   LEFT JOIN ForumThread t ON (t.forum.id = f.id) " +
            "   LEFT JOIN ForumMessage m ON (m.forumThread.id = t.id) " +
            "   WHERE (f.alliance IS NULL OR f.alliance = :alliance) " +
            "   OR ((f.role IS NULL AND f.alliance IS NULL) OR f.role IN (:userRoles))" +
            ")) THEN TRUE ELSE FALSE END FROM ForumMessageRead r " +
            "WHERE r.user.id = :idUser")
    boolean hasUserUnread(@Param("idUser") final int idUser, @Param("userRoles") final Set<EWebUserRole> userRoles, @Param("alliance") final Alliance alliance);
}
