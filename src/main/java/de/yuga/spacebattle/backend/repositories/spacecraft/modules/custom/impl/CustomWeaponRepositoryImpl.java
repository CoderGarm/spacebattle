package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.impl;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomWeaponRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

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
    public List<Weapon> findAllByUser(final int idUser) {
        return em.createNamedQuery("Weapon.getAllByResearches", Weapon.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }
}
