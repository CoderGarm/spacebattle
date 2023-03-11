package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.impl;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomArmorRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

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
    public List<Armor> findAllByUser(final int idUser) {
        return em.createNamedQuery("Armor.getAllByResearches", Armor.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }
}
