package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.impl;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.researches.ResearchLevel;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomWeaponRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomWeaponRepositoryImpl implements CustomWeaponRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Weapon> findAll() {
        return em.createNamedQuery("Weapon.getAll", Weapon.class).getResultList();
    }

    @Nonnull
    @Override
    public List<Weapon> findAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        final Set<Research> researches = user.getResearches().stream().map(ResearchLevel::getResearch).collect(Collectors.toSet());
        if (researches.isEmpty()) {
            return new ArrayList<>();
        }
        return em.createNamedQuery("Weapon.getAllByResearches", Weapon.class)
                .setParameter("researches", researches)
                .getResultList();
    }
}
