package de.yuga.spacebattle.backend.repositories.researches;

import de.yuga.spacebattle.backend.entities.researches.Research;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomResearchRepository {

    @Nonnull
    List<Research> findAll();

    @Nonnull
    List<de.yuga.spacebattle.backend.dto.research.ResearchTreeElement> findAllAsTuple();

    @Nonnull
    List<de.yuga.spacebattle.rest.dto.researches.Research> getResearchesAsDTOById(@Nonnull List<Integer> idResearches);
}
