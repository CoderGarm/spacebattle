package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomTickRepositoryImpl implements CustomTickRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Tick> findAllTicks() {
        final List<Tick> resultList = em.createNamedQuery("Tick.getAll", Tick.class).getResultList();
        return resultList;
    }
}
