package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.StarShipSvgHelper;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.HasNameAndDescriptionDisplayVertical;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.AmmunitionModuleCountDTO;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.PassiveModuleCountDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleMultiDisplay extends VerticalLayout {

    @Nonnull
    private final HasNameAndDescriptionDisplayVertical armorDisplay = new HasNameAndDescriptionDisplayVertical();

    @Nonnull
    private final HasNameAndDescriptionDisplayVertical propulsionDisplay = new HasNameAndDescriptionDisplayVertical();

    @Nonnull
    private final HasNameAndDescriptionDisplayVertical electronicWarfareDisplay = new HasNameAndDescriptionDisplayVertical();

    @Nonnull
    private final HasNameAndDescriptionDisplayVertical sidewallDisplay = new HasNameAndDescriptionDisplayVertical();

    @Nonnull
    private final WeaponAlignmentMultiDisplay bow = new WeaponAlignmentMultiDisplay(EWeaponAlignment.BOW);

    @Nonnull
    private final WeaponAlignmentMultiDisplay stern = new WeaponAlignmentMultiDisplay(EWeaponAlignment.STERN);

    @Nonnull
    private final WeaponAlignmentMultiDisplay broadsides = new WeaponAlignmentMultiDisplay(EWeaponAlignment.BROADSIDE);

    @Nonnull
    private final AmmunitionModuleMultiDisplay ammunitionModuleMultiDisplay = new AmmunitionModuleMultiDisplay();

    @Nonnull
    private final PassiveModuleMultiDisplay passiveModuleMultiDisplay = new PassiveModuleMultiDisplay();

    @Nonnull
    private final StarShipSvgHelper starShipSvgHelper;

    public ModuleMultiDisplay(@Nonnull final StarShipSvgHelper starShipSvgHelper) {
        Preconditions.checkNotNull(starShipSvgHelper, "starShipSvgHelper shouldn't be null!");

        this.starShipSvgHelper = starShipSvgHelper;
        setClassName("module-display");
        add(armorDisplay, propulsionDisplay, electronicWarfareDisplay, sidewallDisplay, bow, stern, broadsides,
                ammunitionModuleMultiDisplay, passiveModuleMultiDisplay);
    }

    /**
     * Will remove not longer existent displays, add new displays or update displays which are already part of this multi display.
     * Or simply clear the full view if the map is null.
     *
     * @param value the ship class to get the modules from
     */
    public void setValue(@Nullable final ShipClass value) {

        if (value == null) {
            clear();
            starShipSvgHelper.calculateBowSlots(3, 0);
            starShipSvgHelper.calculateBroadsideSlots(4, 0);
            starShipSvgHelper.calculateSternSlots(3, 0);
            return;
        }

        final Propulsion propulsion = value.getPropulsion();
        if (propulsion != null) {
            propulsionDisplay.setValue(propulsion);
        }

        final ElectronicWarfare electronicWarfare = value.getElectronicWarfare();
        if (electronicWarfare != null) {
            electronicWarfareDisplay.setValue(electronicWarfare);
        }
        final Sidewall sidewall = value.getSidewall();
        if (sidewall != null) {
            sidewallDisplay.setValue(sidewall);
        }
        final Armor armor = value.getArmor();
        if (armor != null) {
            armorDisplay.setValue(armor);
        }

        final Set<AlignedFitting> alignedFittings = value.getFittings();
        bow.setValue(alignedFittings);
        stern.setValue(alignedFittings);
        broadsides.setValue(alignedFittings);
        final Set<AmmunitionModuleCountDTO> ammunitionModuleCountDTOS = value.getAmmunitionFittings().stream().map(a -> new AmmunitionModuleCountDTO(a.getAmmunitionModule(), a.getAmount())).collect(Collectors.toSet());
        ammunitionModuleMultiDisplay.setValue(ammunitionModuleCountDTOS);
        final Set<PassiveModuleCountDTO> supportFittings = value.getSupportFittings().stream().map(a -> new PassiveModuleCountDTO(a.getPassiveModule(), a.getAmount())).collect(Collectors.toSet());
        passiveModuleMultiDisplay.setValue(supportFittings);

        final int bowAmount = alignedFittings.stream().filter(a -> EWeaponAlignment.BOW == a.getWeaponAlignment()).collect(Collectors.toSet()).size();
        starShipSvgHelper.calculateBowSlots(3, bowAmount);
        final int broadsideAmount = alignedFittings.stream().filter(a -> EWeaponAlignment.BROADSIDE == a.getWeaponAlignment()).collect(Collectors.toSet()).size();
        starShipSvgHelper.calculateBroadsideSlots(4, broadsideAmount);
        final int sternAmount = alignedFittings.stream().filter(a -> EWeaponAlignment.STERN == a.getWeaponAlignment()).collect(Collectors.toSet()).size();
        starShipSvgHelper.calculateSternSlots(3, sternAmount);
    }

    private void clear() {
        getChildren().filter(e -> e instanceof HasValue).forEach(e -> ((HasValue) e).clear());
    }
}
