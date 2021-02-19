package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomTickRepositoryImpl implements CustomTickRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Tick> findAllTicks() {
        final List<Tick> resultList = em.createNamedQuery("Tick.getAll", Tick.class).getResultList();
        return resultList;
    }

    @Nonnull
    @Override
    public Tick getLatest() {
        try {
            return em.createNamedQuery("Tick.getLatest", Tick.class).setMaxResults(1).getSingleResult();
        } catch (final NoResultException e) {
            throw new NotifySBUserException("No first Tick");
        }
    }
}
