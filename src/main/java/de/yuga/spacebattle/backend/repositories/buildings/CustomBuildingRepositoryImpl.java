package de.yuga.spacebattle.backend.repositories.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomBuildingRepositoryImpl implements CustomBuildingRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Building> findAllBuildings() {
        return em.createNamedQuery("Building.getAll", Building.class).getResultList();
    }

    @Nonnull
    @Override
    public List<Building> findBuildingsByProductionTarget(@Nonnull final ProductionType productionTarget) {
        Preconditions.checkNotNull(productionTarget, "productionTarget shouldn't be null!");

        if (productionTarget.getRefinementSequence() == null) {
            return em.createNamedQuery("Building.getByProductionTypeWithoutRefinement", Building.class)
                    .setParameter("productionTarget", productionTarget.getProductionTarget())
                    .setParameter("productionCategory", productionTarget.getProductionCategory())
                    .getResultList();
        }
        return em.createNamedQuery("Building.getByProductionTypeWithRefinement", Building.class)
                .setParameter("productionTarget", productionTarget.getProductionTarget())
                .setParameter("productionCategory", productionTarget.getProductionCategory())
                .setParameter("refinementSequence", productionTarget.getRefinementSequence())
                .getResultList();

    }

    @Nonnull
    @Override
    public List<Building> findAllByUser(final int idUser) {
        return em.createNamedQuery("Building.getAllByResearches", Building.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }
}
