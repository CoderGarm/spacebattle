package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.dto.forum.IdToId;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
public class CustomForumMessageRepositoryImpl implements CustomForumMessageRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<IdToId> findAllMessageIdsForThreadId(final List<Integer> idForumThreads) {
        return em.createNamedQuery("ForumMessage.findAllMessageIdsForThread", IdToId.class)
                .setParameter("idForumThreads", idForumThreads)
                .getResultList();
    }

    @Nonnull
    @Override
    public List<ForumMessage> findMessagesWithPaging(final int idForumThread, final int page, final int size) {
        final int startPosition = page * size;
        final int endPosition = page * size + size;
        return em.createNamedQuery("ForumMessage.findPagedMessages", ForumMessage.class)
                .setParameter("idForumThread", idForumThread)
                .setFirstResult(startPosition)
                .setMaxResults(endPosition)
                .getResultList();
    }

    @Override
    public int countMessagesInThreadById(final int idForumThread) {
        return em.createNamedQuery("ForumMessage.countMessagesInThreadById", Long.class)
                .setParameter("idForumThread", idForumThread)
                .getSingleResult().intValue();
    }
}
