package de.yuga.spacebattle.backend.repositories.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResourceType;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomPlanetRepositoryImpl implements CustomPlanetRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Planet> findAllPlanets() {
        return em.createNamedQuery("Planet.getAll", Planet.class)
                .getResultList();
    }

    @Override
    public List<Planet> findAllOwnedPlanets() {
        return em.createNamedQuery("Planet.getAllOwned", Planet.class)
                .getResultList();
    }

    @Nonnull
    @Override
    public List<Planet> findAllPlanetsColonizedByUser(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        return findAllPlanetsColonizedByID(owner.getId());
    }

    @Nonnull
    @Override
    public List<Planet> findAllPlanetsColonizedByID(final int idUser) {
        return em.createNamedQuery("Planet.getAllOwnedBy", Planet.class)
                .setParameter("idOwner", idUser)
                .getResultList();
    }

    @Nullable
    @Override
    public Planet findResearchPlanet(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        try {
            return em.createNamedQuery("Planet.getPlanetsWithBuildingsForResourceType", Planet.class)
                    .setParameter("owner", owner)
                    .setParameter("resourceType", EResourceType.RESEARCH)
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public Planet findMainPlanetForUser(final int idUser) {
        return em.createNamedQuery("Planet.getMainPlanet", Planet.class)
                .setParameter("idUser", idUser)
                .setMaxResults(1)
                .getSingleResult();
    }

    @Nullable
    @Override
    public Planet findByCoordinates(final int idStarSystem, final Distance xCoordinate, final Distance yCoordinate) {
        try {
            return em.createNamedQuery("Planet.getByCoordinates", Planet.class)
                    .setParameter("idStarSystem", idStarSystem)
                    .setParameter("xCoordinate", new Distance(xCoordinate.getCoordinate(), xCoordinate.getDistanceMetric()))
                    .setParameter("yCoordinate", new Distance(yCoordinate.getCoordinate(), yCoordinate.getDistanceMetric()))
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }
}
