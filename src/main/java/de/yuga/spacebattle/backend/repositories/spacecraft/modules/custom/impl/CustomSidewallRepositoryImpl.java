package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.impl;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomSidewallRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
public class CustomSidewallRepositoryImpl implements CustomSidewallRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Sidewall> findAll() {
        return em.createNamedQuery("Sidewall.getAll", Sidewall.class).getResultList();
    }

    @Nonnull
    @Override
    public List<Sidewall> findAllByUser(final int idUser) {
        return em.createNamedQuery("Sidewall.getAllByResearches", Sidewall.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }
}
