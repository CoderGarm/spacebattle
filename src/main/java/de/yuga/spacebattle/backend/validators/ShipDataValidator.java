package de.yuga.spacebattle.backend.validators;


import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.enums.ECapacityAreaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Transactional
public class ShipDataValidator implements ConstraintValidator<ShipValidator, ShipClass> {

    public void initialize(ShipValidator constraint) {
    }

    public boolean isValid(@Nonnull final ShipClass shipClass, @Nonnull final ConstraintValidatorContext context) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(context, "context shouldn't be null!");

        final Multimap<String, String> errorMap = ArrayListMultimap.create();
        // check if name is valid
        checkName(shipClass, errorMap);
        // check if there is at least the mandatory propulsion module
        checkPropulsion(shipClass, errorMap);
        // check hull
        checkHull(shipClass, errorMap);
        // check if a hull is present
        checkModules(shipClass, errorMap);
        // check predecessor
        checkPredecessor(shipClass, errorMap);
        writeErrors(errorMap, context);
        return errorMap.isEmpty();
    }

    private void writeErrors(@Nonnull final Multimap<String, String> errorMap, @Nonnull final ConstraintValidatorContext context) {
        Preconditions.checkNotNull(errorMap, "errorMap shouldn't be null!");
        Preconditions.checkNotNull(context, "context shouldn't be null!");

        errorMap.forEach((property, error) -> setConstraintViolation(property, error, context));
    }

    public static void checkName(@Nonnull final ShipClass shipClass, @Nonnull final Multimap<String, String> errorMap) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(errorMap, "errorMap shouldn't be null!");

        String name = shipClass.getName();
        if (StringUtils.isBlank(name)) {
            errorMap.put("name", "Name is empty.");
        } else if (name.length() < 3 || name.length() > 30) {
            errorMap.put("name", "Name is to long or to short.");
        }
    }

    public static void checkHull(@Nonnull final ShipClass shipClass, @Nonnull final Multimap<String, String> errorMap) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(errorMap, "errorMap shouldn't be null!");

        if (shipClass.getHull() == null) {
            errorMap.put("Hull", "Hull must not be empty.");
        }
    }

    public static void checkPropulsion(@Nonnull final ShipClass shipClass, @Nonnull final Multimap<String, String> errorMap) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(errorMap, "errorMap shouldn't be null!");

        final Propulsion propulsion = shipClass.getPropulsion();
        if (propulsion == null) {
            errorMap.put("modules", "Needs at least an impeller wedge");
        }
    }

    public static void checkModules(@Nonnull final ShipClass shipClass, @Nonnull final Multimap<String, String> errorMap) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(errorMap, "errorMap shouldn't be null!");

        final Hull hull = shipClass.getHull();
        if (hull != null) {
            // checks if capacity is overridden
            final int usedCapacity = shipClass.getUsedCapacity(ECapacityAreaType.MODULE);
            int constructionCapacity = hull.getConstructionCapacity();
            if (usedCapacity > constructionCapacity) {
                errorMap.put("ConstructionCapacity", "Capacity is exceeded.");
            }

            int usedCapacityBow = shipClass.getUsedCapacity(ECapacityAreaType.BOW);
            final int constructionCapacityBow = hull.getConstructionCapacityBow();
            if (usedCapacityBow > constructionCapacityBow) {
                errorMap.put("ConstructionCapacity Bow", "Capacity is exceeded.");
            }

            int usedCapacityStern = shipClass.getUsedCapacity(ECapacityAreaType.STERN);
            final int constructionCapacityStern = hull.getConstructionCapacityStern();
            if (usedCapacityStern > constructionCapacityStern) {
                errorMap.put("ConstructionCapacity Stern", "Capacity is exceeded.");
            }

            int usedCapacityBroadside = shipClass.getUsedCapacity(ECapacityAreaType.BROADSIDE);
            final int constructionCapacityBroadsides = hull.getConstructionCapacityBroadsides();
            if (usedCapacityBroadside > constructionCapacityBroadsides) {
                errorMap.put("ConstructionCapacity Broadsides", "Capacity is exceeded.");
            }

            int usedCapacityOverall = shipClass.getUsedCapacity(ECapacityAreaType.OVERALL);
            final int constructionCapacityOverall = hull.getOverallConstructionCapacity();
            if (usedCapacityOverall > constructionCapacityOverall) {
                errorMap.put("ConstructionCapacity Overall", "Capacity is exceeded.");
            }
        }
    }

    public static void checkPredecessor(@Nonnull final ShipClass shipClass, @Nonnull final Multimap<String, String> errorMap) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(errorMap, "errorMap shouldn't be null!");

        final User owner = shipClass.getOwner();
        final ShipClass predecessorShipClasses = shipClass.getPredecessor();
        if (predecessorShipClasses != null) {
            if (!predecessorShipClasses.getOwner().equals(owner)) {
                errorMap.put("Predecessors", "You must not edit a foreign ship class.");
            }

            // check if this is it's own predecessor
            if (predecessorShipClasses.equals(shipClass)) {
                errorMap.put("Predecessors", "A ship class cannot be it's own predecessor.");
            }
        }
    }

    private static int addUsedCapacity(int usedCapacity, @Nullable final BaseModule baseModuleWithEffectValue) {
        usedCapacity += baseModuleWithEffectValue != null ? baseModuleWithEffectValue.getUseCapacity() : 0;
        return usedCapacity;
    }

    private static void setConstraintViolation(String property, String msg, ConstraintValidatorContext context) {
        context.buildConstraintViolationWithTemplate(msg).addPropertyNode(property).addConstraintViolation();
    }
}
