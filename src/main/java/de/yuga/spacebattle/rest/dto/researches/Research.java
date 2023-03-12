package de.yuga.spacebattle.rest.dto.researches;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.ETranslationTarget;
import de.yuga.spacebattle.rest.dto.enums.HasIcon;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class Research {

    @Nonnull
    @Schema(required = true, description = "The id of this research.")
    private int idResearch;

    @Nonnull
    @Schema(required = true, description = "The name of this research.")
    private String name;

    @Nonnull
    @Schema(required = true, description = "The description of this research.")
    private String description;

    @Schema(required = true, description = "The maximum level of this research.")
    private int levelCap;

    @Nullable
    @Schema(description = "If it has an icon, then it is described here.")
    private HasIcon hasIcon;

    public Research() {
    }

    public Research(@Nonnull final de.yuga.spacebattle.backend.entities.researches.Research research,
                    @Nonnull final String name,
                    @Nonnull final String description) {
        Preconditions.checkNotNull(research, "research must not be empty");
        Preconditions.checkNotNull(name, "name must not be empty");
        Preconditions.checkNotNull(description, "description must not be empty");

        this.idResearch = research.getId();
        this.name = name;
        this.description = description;
        this.levelCap = research.getLevelCap();
        setIcon(research);
    }

    public Research(@Nonnull final de.yuga.spacebattle.backend.entities.researches.Research research,
                    @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        this.idResearch = research.getId();
        this.name = research.getName(languageCode);
        this.description = research.getDescription(languageCode);
        this.levelCap = research.getLevelCap();
        setIcon(research);
    }

    @JsonIgnore
    private void setIcon(@Nonnull final de.yuga.spacebattle.backend.entities.researches.Research research) {
        Preconditions.checkNotNull(research, "research must not be empty");

        final EResourceType eResourceType = research.getUnlocksBuildings().stream().findFirst().map(Building::getProductionTarget).orElse(null);
        if (eResourceType != null) {
            this.hasIcon = new HasIcon(eResourceType);
            return;
        }

        final EHullType eHullType = research.getUnlocksHulls().stream().findFirst().map(Hull::getHullType).orElse(null);
        if (eHullType != null) {
            this.hasIcon = new HasIcon(eHullType);
            return;
        }

        final ETranslationTarget unlocks = research.getUnlocks();
        switch (unlocks) {
            case HULL:
            case BUILDING:
            case RESEARCH:
            case PASSIVE_MODULE:
                break;
            case WEAPON:
            case MISSILE:
            case LAUNCHER:
                this.hasIcon = new HasIcon(EModuleType.WEAPON);
                break;
            case ARMOR:
                this.hasIcon = new HasIcon(EModuleType.ARMOR);
                break;
            case ELECTRONIC_WARFARE:
                this.hasIcon = new HasIcon(EModuleType.ELECTRONIC_WARFARE);
                break;
            case PROPULSION:
                this.hasIcon = new HasIcon(EModuleType.PROPULSION);
                break;
            case SIDEWALL:
                this.hasIcon = new HasIcon(EModuleType.SIDEWALL);
                break;
        }
    }

    public int getIdResearch() {
        return idResearch;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    public int getLevelCap() {
        return levelCap;
    }

    @Nullable
    public HasIcon getHasIcon() {
        return hasIcon;
    }
}
