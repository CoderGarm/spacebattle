package de.yuga.spacebattle.rest.dto.researches;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.enums.HasIcon;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

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

        final Optional<EResourceType> eResourceType = research.getUnlocksBuildings().stream().findFirst().map(Building::getProductionTarget);
        eResourceType.ifPresent(resourceType -> this.hasIcon = new HasIcon(resourceType));

        final Optional<EHullType> eHullType = research.getUnlocksHulls().stream().findFirst().map(Hull::getHullType);
        eHullType.ifPresent(hullType -> this.hasIcon = new HasIcon(hullType));

        final boolean isArmor = !research.getUnlocksArmor().isEmpty();
        if (isArmor) {
            this.hasIcon = new HasIcon(EModuleType.ARMOR);
        }

        final boolean isPropulsion = !research.getUnlocksPropulsion().isEmpty();
        final boolean isFTLCapable = research.getUnlocksPropulsion().stream().findFirst().map(Propulsion::isFtlCapable).orElse(false);
        if (isPropulsion) {
            if (isFTLCapable) {
                this.hasIcon = new HasIcon(EModuleType.FTLPROPULSION);
            } else {
                this.hasIcon = new HasIcon(EModuleType.PROPULSION);
            }
        }
        final boolean isMissile = !research.getUnlocksMissiles().isEmpty();
        if (isMissile) {
            this.hasIcon = new HasIcon(EModuleType.WEAPON);
        }
        final boolean isEloka = !research.getUnlocksElectronicWarfare().isEmpty();
        if (isEloka) {
            this.hasIcon = new HasIcon(EModuleType.ELECTRONIC_WARFARE);
        }
        final boolean isWeapon = !research.getUnlocksWeapons().isEmpty();
        final boolean isLauncher = !research.getUnlocksLauncher().isEmpty();
        if (isWeapon || isLauncher) {
            this.hasIcon = new HasIcon(EModuleType.WEAPON);
        }
        final boolean isSidewall = !research.getUnlocksSidewall().isEmpty();
        if (isSidewall) {
            this.hasIcon = new HasIcon(EModuleType.SIDEWALL);
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
