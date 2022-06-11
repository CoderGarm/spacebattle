package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
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

    @Nonnull
    @Override
    public List<StarSystem> findAllColonizable() {
        return em.createNamedQuery("StarSystem.getAllColonizable", StarSystem.class).getResultList();
    }

    @Nonnull
    @Override
    public List<StarSystem> findAllColonized() {
        return em.createNamedQuery("StarSystem.getAllColonized", StarSystem.class).getResultList();
    }
}
