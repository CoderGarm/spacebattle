package de.yuga.spacebattle.repositories.orbitals;

import de.yuga.spacebattle.entities.orbitals.Starsystem;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomStarsystemRepositoryImpl implements CustomStarsystemRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Starsystem> findAllStarsystems() {
        final List<Starsystem> resultList = em.createNamedQuery("Starsystem.getAll", Starsystem.class).getResultList();
        return resultList;
    }
}
