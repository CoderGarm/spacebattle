package de.yuga.spacebattle.backend.repositories.spacecraft.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomAmmunitionRepositoryImpl implements CustomAmmunitionRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<AmmunitionModule> findAll() {
        return em.createNamedQuery("AmmunitionModule.getAll", AmmunitionModule.class).getResultList();
    }

    @Nonnull
    @Override
    public List<AmmunitionModule> findAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        final Set<Research> researches = user.getResearches().keySet();
        if (researches.isEmpty()) {
            return new ArrayList<>();
        }
        return em.createNamedQuery("AmmunitionModule.getAllByResearches", AmmunitionModule.class)
                .setParameter("researches", researches)
                .getResultList();
    }
}
