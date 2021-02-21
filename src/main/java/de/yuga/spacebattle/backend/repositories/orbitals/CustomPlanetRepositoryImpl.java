package de.yuga.spacebattle.backend.repositories.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
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

    @Override
    public List<Planet> findAllPlanetsColonizedBy(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return em.createNamedQuery("Planet.getAllOwnedBy", Planet.class)
                .setParameter("owner", user)
                .getResultList();
    }
}
