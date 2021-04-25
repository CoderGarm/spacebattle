package de.yuga.spacebattle.backend.services.researches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.researches.ResearchRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        return researchRepository.findAll();
    }

    @Nullable
    public Research find(@Nonnull final Integer idResearch) {
        Preconditions.checkNotNull(idResearch, "idResearch shouldn't be null!");

        return researchRepository.findById(idResearch).orElse(null);
    }

    /**
     * Returns all researchable researches for the given user.
     * Return includes researches which:
     * - have not reached the level cap
     * - have no restriction
     * - have fulfilled restrictions
     *
     * @param user the user
     * @return all possible researches with their current level
     */
    public Map<Research, Integer> getUnlockableResearches(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        final List<Research> fullResearchList = findAll();
        fullResearchList.removeIf(research -> {
            boolean shouldRemove = false;
            Research unlockedThrough = research.getUnlockedThrough();
            if (unlockedThrough != null && !user.getResearches().containsKey(unlockedThrough)) {
                shouldRemove = true;
            }
            if (user.getResearches().containsKey(research)) {
                shouldRemove = true;
            }
            final Integer currentLevel = user.getResearches().get(research);
            if (currentLevel != null) {
                shouldRemove = research.getLevelCap() <= currentLevel;
            }

            Job runningJob = user.getJobs().stream()
                    .filter(job ->
                            EResourceType.RESEARCH == job.getConstructable().getResourceType()
                                    && job.getConstructable().getResearch() != null
                                    && job.getConstructable().getResearch().equals(research))
                    .findFirst().orElse(null);
            if (runningJob != null) {
                shouldRemove = true;
            }

            return shouldRemove;
        });
        return fullResearchList.stream().collect(Collectors.toMap(research -> research, research -> {
            Integer level = user.getResearches().get(research);
            return Objects.requireNonNullElse(level, 0);
        }));
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
