package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.forum.ForumMessageRead;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface ForumMessageReadRepository extends PagingAndSortingRepository<ForumMessageRead, Integer>, CustomForumMessageReadRepository {

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
    @Query("SELECT CASE WHEN (COUNT(r) <> (SELECT COUNT(m) FROM ForumMessage m)) THEN TRUE ELSE FALSE END FROM ForumMessageRead r " +
            "WHERE r.user.id = :idUser")
    boolean hasUserUnread(@Param("idUser") final int idUser);

}
