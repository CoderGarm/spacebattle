package de.yuga.spacebattle.backend.logic.researches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.repositories.researches.ResearchRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class ResearchService {

    @Nonnull
    private final ResearchRepository researchRepository;

    public ResearchService(@Nonnull final ResearchRepository researchRepository) {
        Preconditions.checkNotNull(researchRepository, "planetRepository shouldn't be null!");

        this.researchRepository = researchRepository;
    }

    @Nonnull
    public List<Research> findAll() {
        return researchRepository.findAllResearchs();
    }

    @Nullable
    public Research find(@Nonnull final Integer idResearch) {
        Preconditions.checkNotNull(idResearch, "idResearch shouldn't be null!");
        return researchRepository.findById(idResearch).orElse(null);
    }

    /**
     * Creates a new {@link Research}.
     *
     * @param name        the name of the research
     * @param description the description
     * @param levelCap    the maximum level of this research
     * @return the new research
     */
    @Nonnull
    public Research createResearch(@Nonnull final String name,
                                   @Nonnull final String description,
                                   final int levelCap,
                                   @Nullable final Research unlockedThrough) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");

        return researchRepository.save(new Research(name, description, levelCap, unlockedThrough));
    }
}
