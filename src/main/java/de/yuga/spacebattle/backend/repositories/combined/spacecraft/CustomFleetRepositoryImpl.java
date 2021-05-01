package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomFleetRepositoryImpl implements CustomFleetRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Fleet> findAllFleets() {
        return em.createNamedQuery("Fleet.getAll", Fleet.class).getResultList();
    }

    @Override
    public List<Fleet> findAllFleetsBy(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return em.createNamedQuery("Fleet.getAllByUser", Fleet.class).setParameter("owner", user).getResultList();
    }

    /**
     * The normal {@link JpaRepository#save(Object)} is not definitely safe to return not null
     * but storing the entity nevertheless. Strange but sadly true.
     *
     * @param fleet the fleet to store
     * @return the stored fleet
     */
    @Override
    public Fleet saveAndFlush(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        if (fleet.getId() != -1) {
            em.merge(fleet);
        } else {
            em.persist(fleet);

        }
        em.flush();
        return fleet;
    }
}
