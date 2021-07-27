package de.yuga.spacebattle.backend.repositories.spacecraft.custom.impl;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.repositories.spacecraft.custom.CustomHullRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomHullRepositoryImpl implements CustomHullRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Hull> findAll() {
        final List<Hull> resultList = em.createNamedQuery("Hull.getAll", Hull.class).getResultList();
        return resultList;
    }

    @Nonnull
    @Override
    public List<Hull> findAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        Set<Research> researches = user.getResearches().keySet();
        if (researches.isEmpty()) {
            return new ArrayList<>();
        }
        return em.createNamedQuery("Hull.getAllByResearches", Hull.class)
                .setParameter("researches", researches)
                .getResultList();
    }
}
