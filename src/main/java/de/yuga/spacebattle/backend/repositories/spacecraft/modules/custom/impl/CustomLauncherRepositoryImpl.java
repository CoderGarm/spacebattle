package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.impl;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomLauncherRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

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
    public List<Launcher> findAllByUser(final int idUser) {
        return em.createNamedQuery("Launcher.getAllByResearches", Launcher.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }
}
