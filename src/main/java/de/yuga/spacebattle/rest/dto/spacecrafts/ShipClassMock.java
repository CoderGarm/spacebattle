package de.yuga.spacebattle.rest.dto.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.rest.dto.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.fittings.SupportFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Armor;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Sidewall;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Schema(description = ".")
public class ShipClassMock implements ShipClassData {

    @Nullable
    @JsonProperty
    @Schema(description = "The name of this class.")
    protected String name;

    @Nullable
    @JsonProperty
    @Schema(description = "The hull of this class.")
    protected Hull hull;

    @Nullable
    @JsonProperty
    @Schema(description = "The propulsion of this class.")
    protected Propulsion propulsion;

    @Nullable
    @JsonProperty
    @Schema(description = "The armor of this class.")
    protected Armor armor;

    @Nullable
    @JsonProperty
    @Schema(description = "The sidewall of this class.")
    protected Sidewall sidewall;

    @Nullable
    @JsonProperty
    @Schema(description = "The electronic warfare module of this class.")
    protected ElectronicWarfare electronicWarfare;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The weapon systems of this class.")
    protected final List<AlignedFitting> fittings = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The ammunition loadout of this class.")
    protected final List<AmmunitionFitting> ammunitionFittings = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The support fitting of this class.")
    protected final List<SupportFitting> supportFittings = new ArrayList<>();

    public ShipClassMock() {
    }

    @Override
    @Nullable
    public String getName() {
        return name;
    }

    @Override
    @Nullable
    public Hull getHull() {
        return hull;
    }

    @Override
    @Nullable
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
}
