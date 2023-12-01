package de.yuga.spacebattle.rest.dto.researches;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import de.yuga.spacebattle.backend.entities.misc.HasCostsByOwn;
import de.yuga.spacebattle.backend.entities.misc.HasName;
import de.yuga.spacebattle.backend.entities.misc.HasNamedTechLevel;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
import de.yuga.spacebattle.rest.dto.enums.HasIcon;
import de.yuga.spacebattle.rest.dto.misc.descriptors.PropertyDescriptor;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Schema(description = ".")
public class ResearchResult {

    public static final EDistanceMetric E_DISTANCE_METRIC = EDistanceMetric.KM;
    public static final EAccelerationMetric E_ACCELERATION_METRIC = EAccelerationMetric.G;
    public static final EMassMetric E_MASS_METRIC = EMassMetric.KT;
    @Nonnull
    @JsonIgnore
    private final String languageCode;

    @JsonProperty
    @Schema(required = true, description = "The level which unlocks this result.")
    private final int unlockedByLevel;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The type of this result.")
    private final ETranslationTarget translationTarget;

    @Nullable
    @JsonProperty
    @Schema(description = "If it has an icon, then it is described here.")
    private HasIcon hasIcon;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this result.")
    private final String name;

    @Nonnull
    @JsonProperty
    @Schema(description = "The technical type name of this result.")
    private String technicalTypeName;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The description of this result.")
    private final String description;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The description of this result.")
    private final Map<String, String> additionalProperties = new HashMap<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The description of this result.")
    private final PropertyDescriptor propertyDescriptor;

    public ResearchResult(final int unlockedThroughLevel, @Nonnull final Building content, @Nonnull final String languageCode) {
        this(unlockedThroughLevel, content, content.getClass(), languageCode);

        final ProductionType pt = content.getProductionType();
        final EResourceType productionTarget = pt.getProductionTarget();
        final EProductionCategory productionCategory = pt.getProductionCategory();
        final ERefinementSequence refinementSequence = pt.getRefinementSequence();
        add("productionTarget", productionTarget.name());
        add("productionCategory", productionCategory.name());
        add("productionCategory", productionCategory.name());
        if (refinementSequence != null) {
            add("refinementSequence", refinementSequence.name());
        }
    }

    public ResearchResult(final int unlockedThroughLevel, @Nonnull final OrbitalModule content, @Nonnull final String languageCode) {
        this(unlockedThroughLevel, content, content.getClass(), languageCode);

        add("system wide effect", content.getEffect().name());
        add("effectValue", content.getBaseValue() + " kpi");
    }

    public ResearchResult(final int unlockedThroughLevel, @Nonnull final Propulsion content, @Nonnull final String languageCode) {
        this(unlockedThroughLevel, content, content.getClass(), languageCode);

        final ETechnologyType technologyType = content.getTechnologyType();
        final EHyperBand hyperBand = content.getHyperBand();
        final int costsPercentage = content.getCostsPercentage();
        add("technologyType", technologyType.name());
        add("hyperBand", hyperBand.name());
        add("costsPercentage", String.valueOf(costsPercentage));
    }

    public ResearchResult(final int unlockedThroughLevel, @Nonnull final PassiveModule content, @Nonnull final String languageCode) {
        this(unlockedThroughLevel, content, content.getClass(), languageCode);

    }

    public ResearchResult(final int unlockedThroughLevel, @Nonnull final Missile content, @Nonnull final String languageCode) {
        this(unlockedThroughLevel, content, content.getClass(), languageCode);

    }

    public ResearchResult(final int unlockedThroughLevel, @Nonnull final Launcher content, @Nonnull final String languageCode) {
        this(unlockedThroughLevel, content, content.getClass(), languageCode);

    }

    public ResearchResult(final int unlockedThroughLevel, @Nonnull final Armor content, @Nonnull final String languageCode) {
        this(unlockedThroughLevel, content, content.getClass(), languageCode);

    }

    public ResearchResult(final int unlockedThroughLevel, @Nonnull final ElectronicWarfare content, @Nonnull final String languageCode) {
        this(unlockedThroughLevel, content, content.getClass(), languageCode);

    }

    public ResearchResult(final int unlockedThroughLevel, @Nonnull final Weapon content, @Nonnull final String languageCode) {
        this(unlockedThroughLevel, content, content.getClass(), languageCode);

    }

    public ResearchResult(final int unlockedThroughLevel, @Nonnull final Sidewall content, @Nonnull final String languageCode) {
        this(unlockedThroughLevel, content, content.getClass(), languageCode);

    }

    private ResearchResult(final int unlockedThroughLevel, @Nonnull final HasName content, @Nonnull final Class<?> clazz, @Nonnull final String languageCode) {
        Preconditions.checkNotNull(content, "content must not be empty");
        Preconditions.checkNotNull(clazz, "clazz must not be empty");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.languageCode = languageCode;
        this.unlockedByLevel = unlockedThroughLevel;
        this.translationTarget = ETranslationTarget.getByClazz(clazz);
        this.hasIcon = HasIcon.getBy(this.translationTarget);
        this.name = content.getName(languageCode);
        this.description = content.getDescription(languageCode);
        this.propertyDescriptor = new PropertyDescriptor(content);
    }

    private ResearchResult(final int unlockedThroughLevel, @Nonnull final HasNamedTechLevel content, @Nonnull final Class<?> clazz, @Nonnull final String languageCode) {
        Preconditions.checkNotNull(content, "content must not be empty");
        Preconditions.checkNotNull(clazz, "clazz must not be empty");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.languageCode = languageCode;
        this.unlockedByLevel = unlockedThroughLevel;
        this.translationTarget = ETranslationTarget.getByClazz(clazz);
        this.hasIcon = HasIcon.getBy(this.translationTarget);
        this.name = content.getName(languageCode);
        this.technicalTypeName = content.getTechnicalTypeName();
        this.description = content.getDescription(languageCode);
        this.propertyDescriptor = new PropertyDescriptor(content);

        if (content instanceof HasCostsByOwn) {
            add("designatedShipClass", ((HasCostsByOwn) content).getShipClassType().name());
            add("effectValue", ((HasCostsByOwn) content).getEffectValue() + " kpi");
        }
    }

    private void add(@Nonnull final String key, @Nonnull final String value) {
        Preconditions.checkNotNull(key, "key must not be empty");
        Preconditions.checkNotNull(value, "value must not be empty");

        additionalProperties.put(key, value);
    }

    public int getUnlockedByLevel() {
        return unlockedByLevel;
    }
}
