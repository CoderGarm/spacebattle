package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomStarSystemRepositoryImpl implements CustomStarSystemRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<StarSystem> findAllStarSystems() {
        return em.createNamedQuery("StarSystem.getAll", StarSystem.class).getResultList();
    }
}
