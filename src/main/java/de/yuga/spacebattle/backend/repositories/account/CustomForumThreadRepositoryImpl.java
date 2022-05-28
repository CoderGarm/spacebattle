package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.dto.forum.IdToId;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomForumThreadRepositoryImpl implements CustomForumThreadRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<IdToId> findAllIdThreadForForums(final List<Integer> idForums) {
        return em.createNamedQuery("ForumThread.findAllIdThreadForForums", IdToId.class)
                .setParameter("idForums", idForums)
                .getResultList();
    }
}
