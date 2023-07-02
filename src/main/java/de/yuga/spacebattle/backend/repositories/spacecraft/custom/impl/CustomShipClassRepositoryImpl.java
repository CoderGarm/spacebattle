package de.yuga.spacebattle.backend.repositories.spacecraft.custom.impl;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.repositories.spacecraft.custom.CustomShipClassRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomShipClassRepositoryImpl implements CustomShipClassRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ShipClass> findAllShipClasses() {
        return em.createNamedQuery("ShipClass.getAll", ShipClass.class).getResultList();
    }

    @Override
    public List<ShipClass> findAllShipClassesByOwner(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return em.createNamedQuery("ShipClass.getAllByOwner", ShipClass.class)
                .setParameter("owner", user)
                .getResultList();
    }

    @Override
    public List<ShipClass> findAllLatestShipClassesByOwner(@Nonnull final Owner user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return em.createNamedQuery("ShipClass.getAllLatestByOwner", ShipClass.class)
                .setParameter("owner", user)
                .getResultList();
    }

    /**
     * <b>Attention:</b>
     * <br>
     * This is relevant and must not be changed until the transient issue is repaired.
     * <br>
     * <br>
     * The normal {@link JpaRepository#save(Object)} is not definitely safe to return not null
     * but storing the entity nevertheless. Strange but sadly true.
     *
     * @param shipClass the ship class to store
     * @return the stored ship class
     */
    @Override
    public ShipClass saveAndFlush(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        if (shipClass.getId() != -1) {
            em.merge(shipClass);
        } else {
            em.persist(shipClass);

        }
        em.flush();
        return shipClass;
    }

    /**
     * Checks if the given class name is known for the user.<br>
     * The name must be between 3 and 30 characters.
     *
     * @param idOwner   the owner's id
     * @param className the class name to check
     * @return <code>true</code> if there are ship classes for this owner and name present, <code>false</code> otherwise
     */
    @Override
    public boolean checkIfClassNameIsFree(final int idOwner, @Nonnull final String className) {
        Preconditions.checkNotNull(className, "className shouldn't be null!");
        Preconditions.checkArgument(className.trim().length() >= 3, "class name is too short");
        Preconditions.checkArgument(className.trim().length() < 30, "class name is too long");

        try {
            final Long amount = em.createNamedQuery("ShipClass.checkIfNameIsFree", Long.class)
                    .setParameter("idOwner", idOwner)
                    .setParameter("className", className)
                    .getSingleResult();
            return amount == 0;
        } catch (final NoResultException e) {
            return false;
        }
    }
}
