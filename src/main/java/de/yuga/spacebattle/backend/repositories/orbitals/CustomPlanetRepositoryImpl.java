package de.yuga.spacebattle.backend.repositories.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResourceType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class CustomPlanetRepositoryImpl implements CustomPlanetRepository {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPlanetRepositoryImpl.class);

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
                    .getResultList().stream()
                    .filter(p -> Objects.nonNull(p.getColonizedAt()))
                    .sorted(Comparator.comparing(Planet::getColonizedAt))
                    .reduce((o1, o2) -> o1)
                    .orElse(null);
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public Planet findMainPlanetForUser(final int idUser) {
        return em.createNamedQuery("Planet.getMainPlanet", Planet.class)
                .setParameter("idUser", idUser)
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
