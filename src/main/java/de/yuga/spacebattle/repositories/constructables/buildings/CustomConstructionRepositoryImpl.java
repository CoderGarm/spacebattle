package de.yuga.spacebattle.repositories.constructables.buildings;

import de.yuga.spacebattle.entities.constructables.buildings.Construction;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomConstructionRepositoryImpl implements CustomConstructionRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Construction> findAllConstructions() {
        final List<Construction> resultList = em.createNamedQuery("Construction.getAll", Construction.class).getResultList();
        return resultList;
    }
}
