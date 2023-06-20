package de.yuga.spacebattle.backend.repositories.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class CustomForumRepositoryImpl implements CustomForumRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public Forum getAllianceForumForUser(@Nonnull final Alliance alliance) {
        Preconditions.checkNotNull(alliance, "alliance shouldn't be null!");

        return em.createNamedQuery("Forum.getAllianceForum", Forum.class)
                .setParameter("alliance", alliance)
                .getSingleResult();
    }
}
