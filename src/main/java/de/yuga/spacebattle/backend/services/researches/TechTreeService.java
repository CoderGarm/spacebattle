package de.yuga.spacebattle.backend.services.researches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.spacecraft.Fitting;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.rest.dto.researches.ResearchTree;
import de.yuga.spacebattle.rest.dto.researches.ResearchTreeElement;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechTreeService {

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final ModuleService moduleService;


    public TechTreeService(@Nonnull final ResearchService researchService,
                           @Nonnull final ModuleService moduleService) {
        Preconditions.checkNotNull(researchService, "researchRepository must not be empty");
        Preconditions.checkNotNull(moduleService, "moduleService must not be empty");

        this.researchService = researchService;
        this.moduleService = moduleService;
    }


    @Nonnull
    public ResearchTree getResearchTree(@Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        final Fitting fitting = moduleService.getFitting();


        final List<Research> all = researchService.findAll();

        final List<ResearchTreeElement> treeElements = all.stream()
                .map(research -> new ResearchTreeElement(research, fitting, languageCode))
                .collect(Collectors.toList());

        for (final ResearchTreeElement treeElement : treeElements) {
            final int idResearch = treeElement.getIdResearch();
            final Integer unlockedBy = treeElement.getIdUnlockedBy();
            if (unlockedBy != null) {
                treeElements.stream()
                        .filter(t -> t.getIdResearch() == unlockedBy)
                        .findFirst().ifPresent(unlockingResearch -> unlockingResearch.setIdUnlocks(idResearch));
            }
        }

        final List<de.yuga.spacebattle.rest.dto.researches.Research> researches = all.stream()
                .map(r -> new de.yuga.spacebattle.rest.dto.researches.Research(r, languageCode))
                .collect(Collectors.toList());

        return new ResearchTree(treeElements, researches);
    }
}
