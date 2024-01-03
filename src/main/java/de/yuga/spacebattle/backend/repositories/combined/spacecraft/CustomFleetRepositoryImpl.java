package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CustomFleetRepositoryImpl implements CustomFleetRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Fleet> findAllFleetsWithoutMovement() {
        return em.createNamedQuery("Fleet.getAllWithoutMovement", Fleet.class).getResultList();
    }

    @Nonnull
    @Override
    public List<Fleet> findAllFleetsWithMovement(final int idUser) {
        return em.createNamedQuery("Fleet.getAllWithMovement", Fleet.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }

    @Nonnull
    @Override
    public List<Fleet> findAllFleetsBy(final int idUser) {
        return em.createNamedQuery("Fleet.getAllByUser", Fleet.class).setParameter("idUser", idUser).getResultList();
    }

    @Nonnull
    @Override
    public List<Fleet> findAllFleetsByStarSystemAndOwner(final int idStarSystem, final int idOwner) {
        return em.createNamedQuery("Fleet.getAllByUserAndSystem", Fleet.class)
                .setParameter("idStarSystem", idStarSystem)
                .setParameter("idOwner", idOwner)
                .getResultList();
    }

    @Nonnull
    @Override
    public Set<Fleet> findAllFleetsByPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        return new HashSet<>(em.createNamedQuery("Fleet.getAllForPlanet", Fleet.class)
                .setParameter("planet", planet)
                .getResultList());
    }

    @Nonnull
    @Override
    public Set<Fleet> findAllAnchoredForPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        return new HashSet<>(em.createNamedQuery("Fleet.getAllAnchoredForPlanet", Fleet.class)
                .setParameter("planet", planet)
                .getResultList());
    }
}
