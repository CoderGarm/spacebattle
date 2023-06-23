package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
public class CustomConstructionRepositoryImpl implements CustomConstructionRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Construction> findAllConstructions() {
        return em.createNamedQuery("Construction.getAll", Construction.class).getResultList();
    }

    @Nonnull
    @Override
    public List<Construction> findAllConstructionsOnPlanet(int idPlanet) {
        return em.createNamedQuery("Construction.getAllByPlanet", Construction.class)
                .setParameter("idPlanet", idPlanet)
                .getResultList();
    }
}
