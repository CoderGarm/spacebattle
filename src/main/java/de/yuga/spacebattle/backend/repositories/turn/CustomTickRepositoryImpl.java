package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Tick;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nullable
    @Override
    public Tick getLatest() {
        try {
            return em.createNamedQuery("Tick.getLatest", Tick.class).setMaxResults(1).getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }
}
