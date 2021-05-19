package de.yuga.spacebattle.backend.validators;


import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Set;

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
        final Set<AlignedFitting> fittings = shipClass.getFittings();

        int usedCapacity = 0;
        usedCapacity = addUsedCapacity(usedCapacity, propulsion);
        usedCapacity = addUsedCapacity(usedCapacity, armor);
        usedCapacity = addUsedCapacity(usedCapacity, electronicWarfare);
        usedCapacity = addUsedCapacity(usedCapacity, sidewall);
        for (AlignedFitting f : fittings) {
            addUsedCapacity(usedCapacity, f.getWeapon());
        }

        int constructionCapacity = shipClass.getHull().getConstructionCapacity();
        if (usedCapacity > constructionCapacity) {
            setConstraintViolation("ConstructionCapacity", "Capacity is overridden.", context);
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
