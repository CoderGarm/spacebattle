package de.yuga.spacebattle.backend.repositories.constructables.spacecraft;

import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomShipClassRepositoryImpl implements CustomShipClassRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ShipClass> findAllShipClasses() {
        final List<ShipClass> resultList = em.createNamedQuery("ShipClass.getAll", ShipClass.class).getResultList();
        return resultList;
    }
}
