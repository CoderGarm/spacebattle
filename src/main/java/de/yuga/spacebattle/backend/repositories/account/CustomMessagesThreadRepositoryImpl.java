package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.chat.MessageThread;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class CustomMessagesThreadRepositoryImpl implements CustomMessageThreadRepository {

    @PersistenceContext
    private EntityManager em;

    @Nullable
    @Override
    public MessageThread findMessagesBetween(final int idUser1, final int idUser2) {
        try {
            return em.createNamedQuery("MessageThread.findMessagesBetween", MessageThread.class)
                    .setParameter("idUser1", idUser1)
                    .setParameter("idUser2", idUser2)
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public List<MessageThread> findThreadsWithUser(final int idUser) {

        return em.createNamedQuery("MessageThread.findThreadsWithUser", MessageThread.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }

    @Nullable
    @Override
    public MessageThread findByIdWithMessages(final int idMessageThread) {
        try {
            return em.createNamedQuery("MessageThread.findByIdWithMessages", MessageThread.class)
                    .setParameter("idMessageThread", idMessageThread)
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }

}
