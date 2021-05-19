package de.yuga.spacebattle.backend.validators;


import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
public class ShipDataValidator implements ConstraintValidator<ShipValidator, ShipClass> {

    public void initialize(ShipValidator constraint) {
    }

    public boolean isValid(ShipClass shipClass, ConstraintValidatorContext context) {

        boolean isError = false;

        // check if name is valid
        String name = shipClass.getName();
        if (StringUtils.isBlank(name)) {
            isError = true;
            setConstraintViolation("name", "Name is empty.", context);
        } else if (name.length() < 3 || name.length() > 30) {
            isError = true;
            setConstraintViolation("name", "Name is to long or to short.", context);
        }

        // check if a hull is present
        if (shipClass.getHull() == null) {
            isError = true;
            setConstraintViolation("hull", "Hull must not be empty.", context);
        }

        // check if there is at least the mandatory propulsion module
        final Propulsion propulsion = shipClass.getPropulsion();
        if (propulsion == null) {
            setConstraintViolation("modules", "Needs at least an impeller wedge", context);
            isError = true;
        }

        // checks is capacity is overridden
        final Armor armor = shipClass.getArmor();
        final ElectronicWarfare electronicWarfare = shipClass.getElectronicWarfare();
        final Sidewall sidewall = shipClass.getSidewall();

        int usedCapacity = 0;
        usedCapacity = addUsedCapacity(usedCapacity, propulsion);
        usedCapacity = addUsedCapacity(usedCapacity, armor);
        usedCapacity = addUsedCapacity(usedCapacity, electronicWarfare);
        usedCapacity = addUsedCapacity(usedCapacity, sidewall);

        int constructionCapacity = shipClass.getHull().getConstructionCapacity();
        if (usedCapacity > constructionCapacity) {
            setConstraintViolation("ConstructionCapacity", "Capacity is overridden.", context);
            isError = true;
        }

        final Set<AlignedFitting> fittings = shipClass.getFittings();
        final Set<AlignedFitting> bowFittings = fittings.stream().filter(f -> EWeaponAlignment.BOW == f.getWeaponAlignment()).collect(Collectors.toSet());
        int usedCapacityBow = 0;
        for (AlignedFitting f : bowFittings) {
            addUsedCapacity(usedCapacityBow, f.getWeapon());
        }
        final int constructionCapacityBow = shipClass.getHull().getConstructionCapacityBow();
        if (usedCapacityBow > constructionCapacityBow) {
            setConstraintViolation("ConstructionCapacity Bow", "Capacity is overridden.", context);
            isError = true;
        }

        final Set<AlignedFitting> sternFittings = fittings.stream().filter(f -> EWeaponAlignment.STERN == f.getWeaponAlignment()).collect(Collectors.toSet());
        int usedCapacityStern = 0;
        for (AlignedFitting f : sternFittings) {
            addUsedCapacity(usedCapacityStern, f.getWeapon());
        }
        final int constructionCapacityStern = shipClass.getHull().getConstructionCapacityStern();
        if (usedCapacityStern > constructionCapacityStern) {
            setConstraintViolation("ConstructionCapacity Stern", "Capacity is overridden.", context);
            isError = true;
        }

        final Set<AlignedFitting> broadsideFittings = fittings.stream().filter(f -> EWeaponAlignment.BROADSIDE == f.getWeaponAlignment()).collect(Collectors.toSet());
        int usedCapacityBroadside = 0;
        for (AlignedFitting f : broadsideFittings) {
            addUsedCapacity(usedCapacityBroadside, f.getWeapon());
        }
        final int constructionCapacityBroadsides = shipClass.getHull().getConstructionCapacityBroadsides();
        if (usedCapacityBroadside > constructionCapacityBroadsides) {
            setConstraintViolation("ConstructionCapacity Broadsides", "Capacity is overridden.", context);
            isError = true;
        }


        return !isError;
    }

    private int addUsedCapacity(int usedCapacity, @Nullable final BaseModule baseModule) {
        usedCapacity += baseModule != null ? baseModule.getUseCapacity() : 0;
        return usedCapacity;
    }

    private static void setConstraintViolation(String property, String msg, ConstraintValidatorContext context) {
        context.buildConstraintViolationWithTemplate(msg).addPropertyNode(property).addConstraintViolation();
    }
}
