package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomStarsystemRepositoryImpl implements CustomStarsystemRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<StarSystem> findAllStarsystems() {
        final List<StarSystem> resultList = em.createNamedQuery("StarSystem.getAll", StarSystem.class).getResultList();
        return resultList;
    }
}
