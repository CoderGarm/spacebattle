package de.yuga.spacebattle.backend.services.researches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.repositories.researches.ResearchRepository;
import de.yuga.spacebattle.rest.dto.researches.ResearchTree;
import de.yuga.spacebattle.rest.dto.researches.ResearchTreeElement;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
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

    @Nonnull
    public ResearchTree getResearchTree() {
        final List<de.yuga.spacebattle.backend.dto.research.ResearchTreeElement> treeElements = findAllAsTuple();
        final Set<Integer> idResearches = treeElements.stream().map(de.yuga.spacebattle.backend.dto.research.ResearchTreeElement::getIdResearch).collect(Collectors.toSet());
        final List<de.yuga.spacebattle.rest.dto.researches.Research> researches = getResearchesAsDTOById(idResearches);

        final Map<Integer, ResearchTreeElement> treeLinkedElementByIdResearch = new HashMap<>();
        // fill up the unlocks und create elements
        for (final de.yuga.spacebattle.backend.dto.research.ResearchTreeElement treeElement : treeElements) {
            final int idResearch = treeElement.getIdResearch();
            final Integer idUnlockedBy = treeElement.getIdUnlockedBy();
            if (idUnlockedBy != null) {
                final ResearchTreeElement element = treeLinkedElementByIdResearch.getOrDefault(idUnlockedBy, new ResearchTreeElement(idUnlockedBy));
                element.setIdUnlocks(idResearch);
                treeLinkedElementByIdResearch.put(idUnlockedBy, element);
            } else {
                treeLinkedElementByIdResearch.put(idResearch, new ResearchTreeElement(idResearch));
            }
        }
        // fill up reverse unlocked by
        final Map<Integer, ResearchTreeElement> avoidConcurrentModification = new HashMap<>();
        for (final ResearchTreeElement treeElement : treeLinkedElementByIdResearch.values()) {
            final Integer idUnlocks = treeElement.getIdUnlocks();
            if (idUnlocks != null) {
                final ResearchTreeElement researchTreeElement = treeLinkedElementByIdResearch.getOrDefault(idUnlocks, new ResearchTreeElement(idUnlocks));
                researchTreeElement.setIdUnlockedBy(treeElement.getIdResearch());
                avoidConcurrentModification.put(idUnlocks, researchTreeElement);
            }
        }
        treeLinkedElementByIdResearch.putAll(avoidConcurrentModification);

        return new ResearchTree(treeLinkedElementByIdResearch.values(), researches);
    }

    @Nonnull
    protected List<de.yuga.spacebattle.backend.dto.research.ResearchTreeElement> findAllAsTuple() {
        return researchRepository.findAllAsTuple();
    }

    @Nonnull
    protected List<de.yuga.spacebattle.rest.dto.researches.Research> getResearchesAsDTOById(@Nonnull final Collection<Integer> idResearches) {
        Preconditions.checkNotNull(idResearches, "idResearches shouldn't be null!");

        return researchRepository.getResearchesAsDTOById(new ArrayList<>(idResearches));
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

        final List<Research> fullResearchList = new ArrayList<>(findAll());
        fullResearchList.removeIf(research -> {
            boolean shouldRemove = false;
            final Research unlockedThrough = research.getUnlockedThrough();
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

            final Job runningJob = user.getJobs().stream()
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
    @Deprecated(since = "productive environment")
    public Research createResearch(@Nonnull final String name,
                                   @Nonnull final String description,
                                   final int levelCap,
                                   @Nonnull final ETechLevel techLevel,
                                   @Nullable final Research unlockedThrough) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(techLevel, "techLevel shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");

        return researchRepository.save(new Research(name, description, levelCap, techLevel, unlockedThrough));
    }
}
