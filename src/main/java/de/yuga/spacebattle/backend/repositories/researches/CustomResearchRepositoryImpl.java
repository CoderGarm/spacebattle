package de.yuga.spacebattle.backend.repositories.researches;

import com.google.common.base.Preconditions;
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

    @Nonnull
    @Override
    public List<de.yuga.spacebattle.rest.dto.researches.Research> getResearchesAsDTOById(@Nonnull final List<Integer> idResearches,
                                                                                         @Nonnull final String languageCode) {
        Preconditions.checkNotNull(idResearches, "idResearches shouldn't be null!");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        return em.createNamedQuery("Research.getResearchesAsDTOById", de.yuga.spacebattle.rest.dto.researches.Research.class)
                .setParameter("idResearches", idResearches)
                .setParameter("languageCode", languageCode)
                .getResultList();
    }
}
