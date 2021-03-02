package de.yuga.spacebattle.backend.repositories.researches;

import de.yuga.spacebattle.backend.entities.researches.Research;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomResearchRepositoryImpl implements CustomResearchRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Research> findAll() {
        final List<Research> resultList = em.createNamedQuery("Research.getAll", Research.class).getResultList();
        return resultList;
    }
}
