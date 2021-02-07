package de.yuga.spacebattle.repositories.combined.spacecraft;

import de.yuga.spacebattle.entities.combined.spacecrafts.Fleet;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomFleetRepositoryImpl implements CustomFleetRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Fleet> findAllFleets() {
        final List<Fleet> resultList = em.createNamedQuery("Fleet.getAll", Fleet.class).getResultList();
        return resultList;
    }


}
