package de.yuga.spacebattle.repositories.spacecraft;

import de.yuga.spacebattle.entities.spacecrafts.Hull;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomHullRepositoryImpl implements CustomHullRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Hull> findAllHulls() {
        final List<Hull> resultList = em.createNamedQuery("Hull.getAll", Hull.class).getResultList();
        return resultList;
    }
}
