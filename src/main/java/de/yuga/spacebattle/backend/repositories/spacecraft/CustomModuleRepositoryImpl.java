package de.yuga.spacebattle.backend.repositories.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomModuleRepositoryImpl implements CustomModuleRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Module> findAll() {
        return em.createNamedQuery("Module.getAll", Module.class).getResultList();
    }

    @Nonnull
    @Override
    public List<Module> findAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        Set<Research> researches = user.getResearches().keySet();
        if (researches.isEmpty()) {
            return new ArrayList<>();
        }
        return em.createNamedQuery("Module.getAllByUser", Module.class)
                .setParameter("researches", researches)
                .getResultList();
    }
}
