package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class CustomConstructionRepositoryImpl implements CustomConstructionRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Construction> findAllConstructions() {
        return em.createNamedQuery("Construction.getAll", Construction.class).getResultList();
    }

    @Nonnull
    @Override
    public List<Construction> findAllConstructionsOnPlanet(int idPlanet) {
        return em.createNamedQuery("Construction.getAllByPlanet", Construction.class)
                .setParameter("idPlanet", idPlanet)
                .getResultList();
    }

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
