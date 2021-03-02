package de.yuga.spacebattle.backend.repositories.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
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

    @Override
    public List<ShipClass> findAllShipClassesByOwner(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return em.createNamedQuery("ShipClass.getAllByOwner", ShipClass.class)
                .setParameter("owner", user)
                .getResultList();
    }
}
