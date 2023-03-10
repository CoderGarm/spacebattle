package de.yuga.spacebattle.backend.validators;

import de.yuga.spacebattle.TestDataProviderUtils;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.enums.ECapacityAreaType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipDataValidatorTest {

    @Test
    void testCapacityCalculation() {

        final ShipClass shipClass = TestDataProviderUtils.shipClass(0);

        final int moduleUsedCapacity = getModuleUsedCapacity(shipClass);
        assertEquals(shipClass.getUsedCapacity(ECapacityAreaType.MODULE), moduleUsedCapacity);
        final int usedCapBow = getUsedCap(shipClass, EWeaponAlignment.BOW);
        assertEquals(shipClass.getUsedCapacity(ECapacityAreaType.BOW), usedCapBow);
        final int usedCapStern = getUsedCap(shipClass, EWeaponAlignment.STERN);
        assertEquals(shipClass.getUsedCapacity(ECapacityAreaType.STERN), usedCapStern);
        final int usedCapBroadsides = getUsedCap(shipClass, EWeaponAlignment.BROADSIDE);
        assertEquals(shipClass.getUsedCapacity(ECapacityAreaType.BROADSIDE), usedCapBroadsides);

        final int usedCap = moduleUsedCapacity + usedCapBow + usedCapStern + usedCapBroadsides;
        assertEquals(shipClass.getUsedCapacity(ECapacityAreaType.OVERALL), usedCap);
    }

    private int getUsedCap(final ShipClass shipClass, final EWeaponAlignment weaponAlignment) {
        final Set<AlignedFitting> fittings = shipClass.getFittings();
        final Set<AlignedFitting> broadsideFittings = fittings.stream().filter(f -> weaponAlignment == f.getWeaponAlignment()).collect(Collectors.toSet());
        int usedCap = 0;
        for (AlignedFitting f : broadsideFittings) {
            int amount = f.getAmount();
            for (; amount > 0; amount--) {
                usedCap += getUsedCapacity(f.getWeapon());
                usedCap += getUsedCapacity(f.getLauncher());
            }
        }
        return usedCap;
    }

    private int getModuleUsedCapacity(final ShipClass shipClass) {
        final Propulsion propulsion = shipClass.getPropulsion();
        final Armor armor = shipClass.getArmor();
        final ElectronicWarfare electronicWarfare = shipClass.getElectronicWarfare();
        final Sidewall sidewall = shipClass.getSidewall();

        final Set<AmmunitionFitting> ammunitionFittings = shipClass.getAmmunitionFittings();
        final Set<SupportFitting> supportFittings = shipClass.getSupportFittings();

        int usedCapacity = 0;
        if (shipClass.getHull() != null) {
            usedCapacity += propulsion != null ? propulsion.getUseCapacity(shipClass.getHull()) : 0;
            usedCapacity += armor != null ? armor.getUseCapacity(shipClass.getHull()) : 0;
            usedCapacity += electronicWarfare != null ? electronicWarfare.getUseCapacity(new Hull()) : 0;
            usedCapacity += getUsedCapacity(sidewall);
        }
        for (AmmunitionFitting fitting : ammunitionFittings) {
            int amount = fitting.getAmount();
            for (; amount > 0; amount--) {
                usedCapacity += getUsedCapacity(fitting.getAmmunitionModule());
            }
        }
        for (SupportFitting fitting : supportFittings) {
            int amount = fitting.getAmount();
            for (; amount > 0; amount--) {
                usedCapacity += getUsedCapacity(fitting.getPassiveModule());
            }
        }
        return usedCapacity;
    }

    private static int getUsedCapacity(@Nullable final BaseModule baseModuleWithEffectValue) {
        return baseModuleWithEffectValue != null ? baseModuleWithEffectValue.getUseCapacity() : 0;
    }
}
