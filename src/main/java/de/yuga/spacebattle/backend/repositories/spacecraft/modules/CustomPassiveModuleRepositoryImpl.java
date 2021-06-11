package de.yuga.spacebattle.backend.repositories.spacecraft.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomPassiveModuleRepositoryImpl implements CustomPassiveModuleRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<PassiveModule> findAll() {
        return em.createNamedQuery("PassiveModule.getAll", PassiveModule.class).getResultList();
    }

    @Nonnull
    @Override
    public List<PassiveModule> findAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        final Set<Research> researches = user.getResearches().keySet();
        if (researches.isEmpty()) {
            return new ArrayList<>();
        }
        return em.createNamedQuery("PassiveModule.getAllByResearches", PassiveModule.class)
                .setParameter("researches", researches)
                .getResultList();
    }
}
