package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Service
public class CustomConstructionRepositoryImpl implements CustomConstructionRepository {

    @PersistenceContext
    private EntityManager em;

    @Nullable
    @Override
    public ResourceDeposit getCosts(final int idBuilding) {

        try {
            return em.createNamedQuery("ResourceDeposit.getCostsForBuilding", ResourceDeposit.class)
                    .setParameter("idBuilding", idBuilding)
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }
}
