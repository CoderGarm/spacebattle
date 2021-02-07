package de.yuga.spacebattle.repositories.buildings;

import de.yuga.spacebattle.entities.buildings.Building;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomBuildingRepositoryImpl implements CustomBuildingRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Building> findAllBuildings() {
        final List<Building> resultList = em.createNamedQuery("Building.getAll", Building.class).getResultList();
        return resultList;
    }
}
