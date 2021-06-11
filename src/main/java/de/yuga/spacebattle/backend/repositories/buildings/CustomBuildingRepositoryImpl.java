package de.yuga.spacebattle.backend.repositories.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.enums.EResourceType;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomBuildingRepositoryImpl implements CustomBuildingRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Building> findAllBuildings() {
        return em.createNamedQuery("Building.getAll", Building.class).getResultList();
    }

    @Nullable
    @Override
    public Building findBuildingByType(@Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        return em.createNamedQuery("Building.getByResourceType", Building.class)
                .setParameter("resourceType", resourceType)
                .getSingleResult();
    }
}
