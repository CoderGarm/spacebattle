package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.impl;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomLauncherRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomLauncherRepositoryImpl implements CustomLauncherRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Launcher> findAll() {
        return em.createNamedQuery("Launcher.getAll", Launcher.class)
                .getResultList();
    }

    @Nonnull
    @Override
    public List<Launcher> findAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        final Set<Research> researches = user.getResearches().keySet();
        if (researches.isEmpty()) {
            return new ArrayList<>();
        }
        return em.createNamedQuery("Launcher.getAllByResearches", Launcher.class)
                .setParameter("researches", researches)
                .getResultList();
    }
}
