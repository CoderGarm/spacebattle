package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomPlanetRepositoryImpl implements CustomPlanetRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Planet> findAllPlanets() {
        final List<Planet> resultList = em.createNamedQuery("Planet.getAll", Planet.class).getResultList();
        return resultList;
    }

    @Override
    public List<Planet> findAllOwnedPlanets() {
        return em.createNamedQuery("Planet.getAllOwned", Planet.class).getResultList();
    }
}
