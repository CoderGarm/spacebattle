package de.yuga.spacebattle.rest.dto.spacecrafts;

import de.yuga.spacebattle.rest.dto.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.details.AmmunitionFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Armor;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Sidewall;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Schema(description = ".", hidden = true)
public interface ShipClassData {

    @Nullable
    String getName();

    @Nullable
    Hull getHull();

    @Nullable
    Propulsion getPropulsion();

    @Nullable
    Armor getArmor();

    @Nullable
    Sidewall getSidewall();

    @Nullable
    ElectronicWarfare getElectronicWarfare();

    @Nonnull
    List<AlignedFitting> getFittings();

    @Nonnull
    List<AmmunitionFitting> getAmmunitionFittings();

    @Nonnull
    List<SupportFitting> getSupportFittings();
}
