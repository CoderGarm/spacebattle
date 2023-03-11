package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.impl;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomPropulsionRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomPropulsionRepositoryImpl implements CustomPropulsionRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Propulsion> findAll() {
        final List<Propulsion> resultList = em.createNamedQuery("Propulsion.getAll", Propulsion.class).getResultList();
        return resultList;
    }

    @Nonnull
    @Override
    public List<Propulsion> findAllByUser(final int idUser) {
        return em.createNamedQuery("Propulsion.getAllByResearches", Propulsion.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }
}
