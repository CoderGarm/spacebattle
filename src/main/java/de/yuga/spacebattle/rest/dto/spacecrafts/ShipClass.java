package de.yuga.spacebattle.rest.dto.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.details.AmmunitionFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Armor;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Sidewall;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShipClass {

    @Nullable
    @ApiModelProperty("The ID.")
    private Integer idShipClass;

    @NotNull
    @Nonnull
    @ApiModelProperty(required = true, value = "The owner of this class.")
    private UserJson owner;

    @NotNull
    @Size(min = 3, max = 30, message = "name should be between 3 and 30 characters long")
    @Nonnull
    @ApiModelProperty(required = true, value = "The name of this class.")
    private String name;

    @NotNull
    @Nonnull
    @ApiModelProperty(required = true, value = "The hull of this class.")
    private Hull hull;

    @NotNull
    @Nullable
    @ApiModelProperty("The propulsion of this class.")
    private Propulsion propulsion;

    @Nullable
    @ApiModelProperty("The armor of this class.")
    private Armor armor;

    @Nullable
    @ApiModelProperty("The sidewall of this class.")
    private Sidewall sidewall;

    @Nullable
    @ApiModelProperty("The electronic warfare module of this class.")
    private ElectronicWarfare electronicWarfare;

    @Nonnull
    @ApiModelProperty(required = true, value = "The weapon systems of this class.")
    private final List<AlignedFitting> fittings = new ArrayList<>();

    @Nonnull
    @ApiModelProperty(required = true, value = "The ammunition loadout of this class.")
    private final List<AmmunitionFitting> ammunitionFittings = new ArrayList<>();

    @Nonnull
    @ApiModelProperty(required = true, value = "The support fitting of this class.")
    private final List<SupportFitting> supportFittings = new ArrayList<>();

    /**
     * The references to the predecessor ship classes in case that this is a class which is another version of their predecessors.
     */
    @Nullable
    @ApiModelProperty(value = "The id of the predecessor of this class.")
    private Integer idPredecessor;

    @Nullable
    @ApiModelProperty(value = "The id of the successor of this class.")
    private Integer idSuccessor;

    @ApiModelProperty(required = true, value = "The mark of this class.")
    private int mark;

    /**
     * Marks if the class is deleted.
     */
    @ApiModelProperty(required = true, value = "If this class is deleted.")
    private boolean isDeleted = false;

    public ShipClass() {
    }

    public ShipClass(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        this.idShipClass = shipClass.getId();
        this.owner = new UserJson(shipClass.getOwner());
        this.name = shipClass.getName();
        if (shipClass.getHull() != null) {
            this.hull = new Hull(shipClass.getHull());
        }
        fittings.addAll(shipClass.getFittings().stream().map(AlignedFitting::new).collect(Collectors.toList()));
        supportFittings.addAll(shipClass.getSupportFittings().stream().map(SupportFitting::new).collect(Collectors.toList()));
        ammunitionFittings.addAll(shipClass.getAmmunitionFittings().stream().map(AmmunitionFitting::new).collect(Collectors.toList()));

        if (shipClass.getPropulsion() != null) {
            this.propulsion = new Propulsion(shipClass.getPropulsion());
        }
        if (shipClass.getArmor() != null) {
            this.armor = new Armor(shipClass.getArmor());
        }
        if (shipClass.getSidewall() != null) {
            this.sidewall = new Sidewall(shipClass.getSidewall());
        }
        if (shipClass.getElectronicWarfare() != null) {
            this.electronicWarfare = new ElectronicWarfare(shipClass.getElectronicWarfare());
        }
        if (shipClass.getSuccessor() != null) {
            this.idSuccessor = shipClass.getSuccessor().getId();
        }
        if (shipClass.getPredecessor() != null) {
            this.idPredecessor = shipClass.getPredecessor().getId();
        }
        this.mark = shipClass.getMark();
        this.isDeleted = shipClass.isDeleted();
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
