package de.yuga.spacebattle.rest.dto.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapabilities;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapacityAreas;
import de.yuga.spacebattle.rest.dto.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.details.AmmunitionFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.details.SupportFitting;
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
import java.util.stream.Collectors;

@Schema(description = ".")
public class ShipClass {

    @Nullable
    @Schema(description = "The ID.")
    private Integer idShipClass;

    @Nonnull
    @Schema(required = true, description = "The owner of this class.")
    private UserJson owner;

    @Nonnull
    @Schema(required = true, description = "The name of this class.")
    @Size(min = 3, max = 30, message = "name should be between 3 and 30 characters long")
    private String name;

    @Nonnull
    @Schema(required = true, description = "The hull of this class.")
    private Hull hull;

    @Nullable
    @Schema(description = "The propulsion of this class.")
    private Propulsion propulsion;

    @Nullable
    @Schema(description = "The armor of this class.")
    private Armor armor;

    @Nullable
    @Schema(description = "The sidewall of this class.")
    private Sidewall sidewall;

    @Nullable
    @Schema(description = "The electronic warfare module of this class.")
    private ElectronicWarfare electronicWarfare;

    @Nonnull
    @Schema(required = true, description = "The weapon systems of this class.")
    private final List<AlignedFitting> fittings = new ArrayList<>();

    @Nonnull
    @Schema(required = true, description = "The ammunition loadout of this class.")
    private final List<AmmunitionFitting> ammunitionFittings = new ArrayList<>();

    @Nonnull
    @Schema(required = true, description = "The support fitting of this class.")
    private final List<SupportFitting> supportFittings = new ArrayList<>();

    /**
     * The references to the predecessor ship classes in case that this is a class which is another version of their predecessors.
     */
    @Nullable
    @Schema(description = "The id of the predecessor of this class.")
    private Integer idPredecessor;

    @Nullable
    @Schema(description = "The id of the successor of this class.")
    private Integer idSuccessor;

    @Schema(required = true, description = "The mark of this class.")
    private int mark;

    /**
     * Marks if the class is deleted.
     */
    @Schema(required = true, description = "If this class is deleted.")
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
        this.owner = new UserJson(shipClass.getOwner());
        this.name = shipClass.getName();
        if (shipClass.getHull() != null) {
            this.hull = new Hull(shipClass.getHull(), languageCode);
        }
        fittings.addAll(shipClass.getFittings().stream().map(a -> new AlignedFitting(a, languageCode)).collect(Collectors.toList()));
        supportFittings.addAll(shipClass.getSupportFittings().stream().map(s -> new SupportFitting(s, languageCode)).collect(Collectors.toList()));
        ammunitionFittings.addAll(shipClass.getAmmunitionFittings().stream().map(a -> new AmmunitionFitting(a, languageCode)).collect(Collectors.toList()));

        if (shipClass.getPropulsion() != null) {
            this.propulsion = new Propulsion(shipClass.getPropulsion(), languageCode);
        }
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
        this.mark = shipClass.getMark();
        this.isDeleted = shipClass.isDeleted();
        this.shipClassCapabilities = new SpacecraftCapabilities(shipClass);
        this.spacecraftCapacityAreas = new SpacecraftCapacityAreas(shipClass);
        this.ammunitionState = new MissileAmmunitionState(shipClass.getAmmunitionFittings(), languageCode);
    }

    @Nullable
    public Integer getIdShipClass() {
        return idShipClass;
    }

    @Nonnull
    public UserJson getOwner() {
        return owner;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Hull getHull() {
        return hull;
    }

    @Nullable
    public Propulsion getPropulsion() {
        return propulsion;
    }

    @Nullable
    public Armor getArmor() {
        return armor;
    }

    @Nullable
    public Sidewall getSidewall() {
        return sidewall;
    }

    @Nullable
    public ElectronicWarfare getElectronicWarfare() {
        return electronicWarfare;
    }

    @Nonnull
    public List<AlignedFitting> getFittings() {
        return fittings;
    }

    @Nonnull
    public List<AmmunitionFitting> getAmmunitionFittings() {
        return ammunitionFittings;
    }

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
