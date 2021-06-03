package de.yuga.spacebattle.backend.repositories.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomColonizationRepositoryImpl implements CustomColonizationRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Colonization> findAll() {
        return em.createNamedQuery("Colonization.getAll", Colonization.class).getResultList();
    }

    @Override
    public List<Colonization> findAllForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return em.createNamedQuery("Colonization.getAllForUser", Colonization.class)
                .setParameter("user", user)
                .getResultList();
    }
}
