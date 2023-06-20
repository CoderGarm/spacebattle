package de.yuga.spacebattle.backend.repositories.researches;

import de.yuga.spacebattle.backend.entities.researches.Research;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
public class CustomResearchRepositoryImpl implements CustomResearchRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Research> findAll() {
        return em.createNamedQuery("Research.getAll", Research.class).getResultList();
    }
}
