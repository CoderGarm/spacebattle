package de.yuga.spacebattle.rest.dto.researches;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.spacecraft.Fitting;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.ETranslationTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "The ids of a research by the id of the unlocking research and the id of an unlocked research.")
public class ResearchTreeElement {

    @JsonProperty
    @Schema(required = true, description = "The id of this research.")
    private int idResearch;

    @Nullable
    @JsonProperty
    @Schema(description = "The id of the research which unlocks this research.")
    private Integer idUnlockedBy;

    @Nullable
    @JsonProperty
    @Schema(description = "The id of the research which is unlocked by this research.")
    private Integer idUnlocks;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The important levels for this research.")
    private final List<Integer> levels = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The results which are unlocked by this research.")
    private final List<ResearchResult> unlocks = new ArrayList<>();

    @JsonIgnore
    private String languageCode;

    public ResearchTreeElement(final int idResearch) {
        this.idResearch = idResearch;
    }

    public ResearchTreeElement(@Nonnull final Research research, @Nonnull final Fitting fitting, @Nonnull final String languageCode) {
        Preconditions.checkNotNull(research, "research must not be empty");
        Preconditions.checkNotNull(fitting, "fitting must not be empty");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.idResearch = research.getId();
        this.idUnlockedBy = research.getUnlockedThrough() != null ? research.getUnlockedThrough().getId() : null;
        this.setResearch(research, fitting, languageCode);
    }

    @JsonIgnore
    public int getIdResearch() {
        return idResearch;
    }

    @Nullable
    @JsonIgnore
    public Integer getIdUnlockedBy() {
        return idUnlockedBy;
    }

    @Nullable
    @JsonIgnore
    public Integer getIdUnlocks() {
        return idUnlocks;
    }

    @JsonIgnore
    public void setIdUnlockedBy(@Nullable final Integer idUnlockedBy) {
        this.idUnlockedBy = idUnlockedBy;
    }

    @JsonIgnore
    public void setIdUnlocks(@Nullable final Integer idUnlocks) {
        this.idUnlocks = idUnlocks;
    }

    @JsonIgnore
    public boolean matchesIdResearch(final int idResearch) {
        return this.idResearch == idResearch;
    }

    @JsonIgnore
    public boolean isPartOfChain(@Nonnull final Integer idResearch) {
        Preconditions.checkNotNull(idResearch, "idResearch must not be empty");

        return this.idResearch == idResearch || idResearch.equals(this.idUnlockedBy) || idResearch.equals(this.idUnlocks);
    }

    private void setResearch(@Nonnull final Research research, @Nonnull final Fitting fitting, @Nonnull final String languageCode) {
        Preconditions.checkNotNull(research, "research must not be empty");
        Preconditions.checkNotNull(fitting, "fitting must not be empty");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.languageCode = languageCode;
        research.getUnlocksBuildings().forEach(b -> add(b.getUnlockedThroughLevel(), b));
        research.getUnlocksPassiveModules().forEach(b -> add(b.getUnlockedThroughLevel(), b));

        final Set<NamedTechLevel> unlocksNamedTechLevel = research.getUnlocksNamedTechLevel();
        final Map<ETranslationTarget, List<NamedTechLevel>> byType = unlocksNamedTechLevel.stream().collect(Collectors.groupingBy(NamedTechLevel::getTranslationTarget, Collectors.toList()));
        byType.forEach((eTranslationTarget, namedTechLevels) -> {
            namedTechLevels.forEach(namedTechLevel -> {
                switch (eTranslationTarget) {
                    case BUILDING:
                    case RESEARCH:
                    case PASSIVE_MODULE:
                        // noop - both already covered
                        break;
                    case MISSILE:
                    case LAUNCHER:
                        fitting.getMissiles().forEach((launcher, missile) -> {
                            if (missile.getNamedTechLevel().equals(namedTechLevel)) {
                                add(missile.getUnlockedThroughLevel(), missile);
                            }
                            if (launcher.getNamedTechLevel().equals(namedTechLevel)) {
                                add(launcher.getUnlockedThroughLevel(), launcher);
                            }
                        });
                        break;
                    case ARMOR:
                        fitting.getArmors().stream().filter(a -> a.getNamedTechLevel().equals(namedTechLevel)).forEach(a -> add(a.getUnlockedThroughLevel(), a));
                        break;
                    case ELECTRONIC_WARFARE:
                        fitting.getEloka().stream().filter(a -> a.getNamedTechLevel().equals(namedTechLevel)).forEach(a -> add(a.getUnlockedThroughLevel(), a));
                        break;
                    case PROPULSION:
                        fitting.getPropulsions().stream().filter(a -> a.getNamedTechLevel().equals(namedTechLevel)).forEach(a -> add(a.getUnlockedThroughLevel(), a));
                        break;
                    case WEAPON:
                        fitting.getWeapons().stream().filter(a -> a.getNamedTechLevel().equals(namedTechLevel)).forEach(a -> add(a.getUnlockedThroughLevel(), a));
                        break;
                    case SIDEWALL:
                        fitting.getSidewalls().stream().filter(a -> a.getNamedTechLevel().equals(namedTechLevel)).forEach(a -> add(a.getUnlockedThroughLevel(), a));
                        break;
                }
            });
        });

        this.levels.addAll(this.unlocks.stream()
                .map(ResearchResult::getUnlockedByLevel)
                .collect(Collectors.toSet()).stream()
                .sorted(Integer::compare)
                .collect(Collectors.toList()));
    }

    private void add(final int unlockedThroughLevel, final PassiveModule content) {
        Preconditions.checkNotNull(content, "content must not be empty");
        this.unlocks.add(new ResearchResult(unlockedThroughLevel, content, languageCode));
    }

    private void add(final int unlockedThroughLevel, final Propulsion content) {
        Preconditions.checkNotNull(content, "content must not be empty");
        this.unlocks.add(new ResearchResult(unlockedThroughLevel, content, languageCode));
    }

    private void add(final int unlockedThroughLevel, final Sidewall content) {
        Preconditions.checkNotNull(content, "content must not be empty");
        this.unlocks.add(new ResearchResult(unlockedThroughLevel, content, languageCode));
    }

    private void add(final int unlockedThroughLevel, final Weapon content) {
        Preconditions.checkNotNull(content, "content must not be empty");
        this.unlocks.add(new ResearchResult(unlockedThroughLevel, content, languageCode));
    }

    private void add(final int unlockedThroughLevel, final ElectronicWarfare content) {
        Preconditions.checkNotNull(content, "content must not be empty");
        this.unlocks.add(new ResearchResult(unlockedThroughLevel, content, languageCode));
    }

    private void add(final int unlockedThroughLevel, final Armor content) {
        Preconditions.checkNotNull(content, "content must not be empty");
        this.unlocks.add(new ResearchResult(unlockedThroughLevel, content, languageCode));
    }

    private void add(final int unlockedThroughLevel, final Launcher content) {
        Preconditions.checkNotNull(content, "content must not be empty");
        this.unlocks.add(new ResearchResult(unlockedThroughLevel, content, languageCode));
    }

    private void add(final int unlockedThroughLevel, final Missile content) {
        Preconditions.checkNotNull(content, "content must not be empty");
        this.unlocks.add(new ResearchResult(unlockedThroughLevel, content, languageCode));
    }

    private void add(final int unlockedThroughLevel, final Building content) {
        Preconditions.checkNotNull(content, "content must not be empty");
        this.unlocks.add(new ResearchResult(unlockedThroughLevel, content, languageCode));
    }

    @Override
    @JsonIgnore
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final ResearchTreeElement that = (ResearchTreeElement) o;

        return new EqualsBuilder().append(idResearch, that.idResearch).isEquals();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(idResearch).toHashCode();
    }
}
