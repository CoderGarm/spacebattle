package de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.impl;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomElectronicWarfareRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
public class CustomElectronicWarfareRepositoryImpl implements CustomElectronicWarfareRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<ElectronicWarfare> findAll() {
        return em.createNamedQuery("ElectronicWarfare.getAll", ElectronicWarfare.class).getResultList();
    }

    @Nonnull
    @Override
    public List<ElectronicWarfare> findAllByUser(final int idUser) {
        return em.createNamedQuery("ElectronicWarfare.getAllByResearches", ElectronicWarfare.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }
}
