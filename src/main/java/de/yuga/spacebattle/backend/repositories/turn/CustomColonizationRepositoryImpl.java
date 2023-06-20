package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Colonization;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

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
    public List<Colonization> findAllForUser(final int idUser) {
        return em.createNamedQuery("Colonization.getAllForUser", Colonization.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }
}
