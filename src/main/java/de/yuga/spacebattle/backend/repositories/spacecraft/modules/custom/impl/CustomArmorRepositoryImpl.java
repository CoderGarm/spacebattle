package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.impl;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomArmorRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomArmorRepositoryImpl implements CustomArmorRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Armor> findAll() {
        final List<Armor> resultList = em.createNamedQuery("Armor.getAll", Armor.class).getResultList();
        return resultList;
    }

    @Nonnull
    @Override
    public List<Armor> findAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        final Set<Research> researches = user.getResearches().keySet();
        if (researches.isEmpty()) {
            return new ArrayList<>();
        }
        return em.createNamedQuery("Armor.getAllByResearches", Armor.class)
                .setParameter("researches", researches)
                .getResultList();
    }
}
