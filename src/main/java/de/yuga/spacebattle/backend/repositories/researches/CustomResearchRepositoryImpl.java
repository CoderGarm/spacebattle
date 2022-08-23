package de.yuga.spacebattle.backend.repositories.researches;

import de.yuga.spacebattle.backend.entities.researches.Research;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    @Nonnull
    @Override
    public List<de.yuga.spacebattle.backend.dto.research.ResearchTreeElement> findAllAsTuple() {
        return em.createNamedQuery("Research.getTreeAsTuple", de.yuga.spacebattle.backend.dto.research.ResearchTreeElement.class).getResultList();
    }
}
