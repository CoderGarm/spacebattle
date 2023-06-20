package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessageRead;
import de.yuga.spacebattle.backend.entities.account.forum.ForumThread;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

@Service
public class CustomForumMessageReadRepositoryImpl implements CustomForumMessageReadRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public ForumMessageRead create(final int idForum,
                                   final int idForumThread,
                                   final int idForumMessage,
                                   final int idUser) {
        final Forum forum = em.getReference(Forum.class, idForum);
        final ForumThread forumThread = em.getReference(ForumThread.class, idForumThread);
        final ForumMessage forumMessage = em.getReference(ForumMessage.class, idForumMessage);
        final User user = em.getReference(User.class, idUser);
        return new ForumMessageRead(forum, forumThread, forumMessage, user);
    }
}
