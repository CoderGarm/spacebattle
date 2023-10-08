package de.yuga.spacebattle.rest.dto.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.rest.dto.account.Player;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapabilities;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapacityAreas;
import de.yuga.spacebattle.rest.dto.enums.EShipClassType;
import de.yuga.spacebattle.rest.dto.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.fittings.SupportFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Armor;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.rest.dto.turn.battle.combat.MissileAmmunitionState;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class ShipClass implements ShipClassData {

    /**
     * The Types which must not be displayed as effect value in any statistics display.
     */
    @Nonnull
    private static final Set<de.yuga.spacebattle.rest.dto.enums.EModuleType> FORBIDDEN_TYPES = Set.of(
            new de.yuga.spacebattle.rest.dto.enums.EModuleType(EModuleType.PROPULSION),
            new de.yuga.spacebattle.rest.dto.enums.EModuleType(EModuleType.FTLPROPULSION)
    );

    @Nullable
    @JsonProperty
    @Schema(description = "The ID.")
    private Integer idShipClass;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The owner of this class.")
    private Player owner;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this class.")
    @Size(min = 3, max = 30, message = "name should be between 3 and 30 characters long")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The strategic usage of this class.")
    protected EShipClassType shipClassType;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The propulsion of this class.")
    private Propulsion propulsion;

    @Nullable
    @JsonProperty
    @Schema(description = "The armor of this class.")
    private Armor armor;

    @Nullable
    @JsonProperty
    @Schema(description = "The sidewall of this class.")
    private Sidewall sidewall;

    @Nullable
    @JsonProperty
    @Schema(description = "The electronic warfare module of this class.")
    private ElectronicWarfare electronicWarfare;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The weapon systems of this class.")
    private final List<AlignedFitting> fittings = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The ammunition loadout of this class.")
    private final List<AmmunitionFitting> ammunitionFittings = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The support fitting of this class.")
    private final List<SupportFitting> supportFittings = new ArrayList<>();

    /**
     * The references to the predecessor ship classes in case that this is a class which is another version of their predecessors.
     */
    @Nullable
    @JsonProperty
    @Schema(description = "The id of the predecessor of this class.")
    private Integer idPredecessor;

    @Nullable
    @JsonProperty
    @Schema(description = "The id of the successor of this class.")
    private Integer idSuccessor;

    @JsonProperty
    @Schema(required = true, description = "The mark of this class.")
    private int mark;

    /**
     * Marks if the class is deleted.
     */
    @JsonProperty
    @Schema(description = "If this class is deleted.")
    private boolean isDeleted = false;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The effect values per module type.")
    private SpacecraftCapabilities shipClassCapabilities;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The capacities used per area.")
    private SpacecraftCapacityAreas spacecraftCapacityAreas;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The aggregated missile load out.")
    private MissileAmmunitionState ammunitionState;

    public ShipClass() {
    }

    public ShipClass(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass shipClass,
                     @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        this.idShipClass = shipClass.getId();
        this.owner = new Player(shipClass.getOwner());
        this.name = shipClass.getName();
        this.shipClassType = new EShipClassType(shipClass.getShipClassType());
        fittings.addAll(shipClass.getFittings().stream().map(a -> new AlignedFitting(a, languageCode)).collect(Collectors.toList()));
        supportFittings.addAll(shipClass.getSupportFittings().stream().map(s -> new SupportFitting(s, languageCode)).collect(Collectors.toList()));
        ammunitionFittings.addAll(shipClass.getAmmunitionFittings().stream().map(a -> new AmmunitionFitting(a, languageCode)).collect(Collectors.toList()));

        this.propulsion = new Propulsion(shipClass.getPropulsion(), languageCode);
        if (shipClass.getArmor() != null) {
            this.armor = new Armor(shipClass.getArmor(), languageCode);
        }
        if (shipClass.getSidewall() != null) {
            this.sidewall = new Sidewall(shipClass.getSidewall(), languageCode);
        }
        if (shipClass.getElectronicWarfare() != null) {
            this.electronicWarfare = new ElectronicWarfare(shipClass.getElectronicWarfare(), languageCode);
        }
        if (shipClass.getSuccessor() != null) {
            this.idSuccessor = shipClass.getSuccessor().getId();
        }
        if (shipClass.getPredecessor() != null) {
            this.idPredecessor = shipClass.getPredecessor().getId();
        }
        this.mark = shipClass.getFlight();
        this.isDeleted = shipClass.isDeleted();
        this.shipClassCapabilities = new SpacecraftCapabilities(shipClass);
        this.shipClassCapabilities.getCapabilities().removeIf(capabilityValue -> FORBIDDEN_TYPES.contains(capabilityValue.getModuleType()));
        this.spacecraftCapacityAreas = new SpacecraftCapacityAreas(shipClass);
        this.ammunitionState = new MissileAmmunitionState(shipClass.getAmmunitionFittings(), languageCode);
    }

    @Nullable
    public Integer getIdShipClass() {
        return idShipClass;
    }

    @Nonnull
    public Player getOwner() {
        return owner;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nonnull
    public EShipClassType getShipClassType() {
        return shipClassType;
    }

    @Override
    @Nonnull
    public Propulsion getPropulsion() {
        return propulsion;
    }

    @Override
    @Nullable
    public Armor getArmor() {
        return armor;
    }

    @Override
    @Nullable
    public Sidewall getSidewall() {
        return sidewall;
    }

    @Override
    @Nullable
    public ElectronicWarfare getElectronicWarfare() {
        return electronicWarfare;
    }

    @Override
    @Nonnull
    public List<AlignedFitting> getFittings() {
        return fittings;
    }

    @Override
    @Nonnull
    public List<AmmunitionFitting> getAmmunitionFittings() {
        return ammunitionFittings;
    }

    @Override
    @Nonnull
    public List<SupportFitting> getSupportFittings() {
        return supportFittings;
    }

    @Nullable
    public Integer getIdPredecessor() {
        return idPredecessor;
    }

    @Nullable
    public Integer getIdSuccessor() {
        return idSuccessor;
    }

    public int getMark() {
        return mark;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return isDeleted;
    }
}
