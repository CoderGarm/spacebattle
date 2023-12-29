package de.yuga.spacebattle.backend.services.researches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.researches.ResearchLevel;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.repositories.researches.ResearchLevelRepository;
import de.yuga.spacebattle.backend.repositories.researches.ResearchRepository;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResearchService {

    @Nonnull
    private final ResearchRepository researchRepository;

    @Nonnull
    private final ResearchLevelRepository levelRepository;

    public ResearchService(@Nonnull final ResearchRepository researchRepository,
                           @Nonnull final ResearchLevelRepository levelRepository) {
        Preconditions.checkNotNull(researchRepository, "planetRepository shouldn't be null!");
        Preconditions.checkNotNull(levelRepository, "levelRepository shouldn't be null!");

        this.researchRepository = researchRepository;
        this.levelRepository = levelRepository;
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

    @Nonnull
    public Set<ResearchLevel> getResearchesForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        final Set<ResearchLevel> allForUser = levelRepository.getAllForUser(user.getId());
        return Objects.requireNonNullElse(allForUser, new HashSet<>());
    }

    @Nonnull
    public Set<ResearchLevel> getResearchesForUser(final int idUser) {
        final Set<ResearchLevel> allForUser = levelRepository.getAllForUser(idUser);
        return Objects.requireNonNullElse(allForUser, new HashSet<>());
    }

    public Set<ResearchLevel> getResearchesForUser(final int idUser, @Nonnull final Set<Research> researches) {
        Preconditions.checkNotNull(researches, "researches must not be empty");

        final Set<ResearchLevel> allForUser = levelRepository.getResearchLevelsFor(idUser, researches.stream().map(Research::getId).collect(Collectors.toSet()));
        return Objects.requireNonNullElse(allForUser, new HashSet<>());
    }

    /**
     * Returns all researchable researches for the given user.
     * Return includes researches which:
     * - have not reached the level cap
     * - have no restriction
     * - have fulfilled restrictions
     *
     * @param idUser the id of the user
     * @return all possible researches with their next level
     */
    public Map<Research, Integer> getUnlockableResearches(final int idUser, final List<Research> jobActiveFor) {

        final Set<ResearchLevel> levels = getResearchesForUser(idUser);

        final Map<Research, Integer> levelsByResearch = levels.stream().collect(Collectors.toMap(ResearchLevel::getResearch, ResearchLevel::getLevel));
        final List<Research> fullResearchList = new ArrayList<>(findAll());

        fullResearchList.removeIf(research -> {

            final Research unlockedThrough = research.getUnlockedThrough();
            if (unlockedThrough != null && !levelsByResearch.containsKey(unlockedThrough)) {
                return true;
            }
            final Integer researchLevel = levelsByResearch.get(research);
            if (researchLevel != null) {
                return research.getLevelCap() <= researchLevel;
            }

            final Research runningJob = jobActiveFor.stream().filter(j -> j.equals(research)).findFirst().orElse(null);
            return runningJob != null;
        });
        return fullResearchList.stream().collect(Collectors.toMap(research -> research, research -> {
            final Integer level = levelsByResearch.get(research);
            return Objects.requireNonNullElse(level, 0) + 1;
        }));
    }

    public void addResearchForNewAccounts(@Nonnull final User entity, @Nonnull final List<Research> researches) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");
        Preconditions.checkNotNull(researches, "researches shouldn't be null!");

        addResearch(entity, researches);
        addResearch(entity, researches);
    }

    public void addResearch(@Nonnull final User entity, @Nonnull final List<Research> researches) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");
        Preconditions.checkNotNull(researches, "researches shouldn't be null!");

        final Set<ResearchLevel> allForUser = getResearchesForUser(entity.getId());
        for (Research research : researches) {
            final ResearchLevel researchLevel = allForUser.stream()
                    .filter(rl -> rl.getResearch().equals(research))
                    .findFirst()
                    .orElse(null);
            if (researchLevel != null) {
                final int currentLevel = researchLevel.getLevel();
                final int nextLevel = currentLevel + 1;
                if (nextLevel <= research.getLevelCap()) {
                    researchLevel.setLevel(nextLevel);
                }
            } else {
                allForUser.add(new ResearchLevel(entity, research));
            }
        }
        levelRepository.saveAll(allForUser);
    }

    @Nonnull
    public List<Research> getResearchesWithoutPrecondition() {
        final List<Research> researchesWithoutPrecondition = researchRepository.getResearchesWithoutPrecondition();
        return Objects.requireNonNullElseGet(researchesWithoutPrecondition, ArrayList::new);
    }

    public boolean isResearchAtLevelCap(@Nonnull final User user, @Nonnull final Research research) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        return levelRepository.isResearchUnlocked(user.getId(), research.getId());
    }

    public int getLevelForResearch(@Nonnull final User user, @Nonnull final Research research) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        final ResearchLevel researchLevel = levelRepository.getResearchLevelFor(user.getId(), research.getId());
        if (researchLevel == null) {
            return 0;
        }
        return researchLevel.getLevel();
    }

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

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public Research save(@Nonnull final Research research) {
        Preconditions.checkNotNull(research, "research must not be empty");

        return researchRepository.save(research);
    }

    public void deleteAll(@Nonnull final Set<ResearchLevel> researchLevels) {
        Preconditions.checkNotNull(researchLevels, "researchLevels must not be empty");

        levelRepository.deleteAll(researchLevels);
    }
}
