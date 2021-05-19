package de.yuga.spacebattle.gui.vaadin.validators;


import com.vaadin.flow.data.binder.ErrorLevel;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ShipDataVaadinValidator implements Validator<ShipClass> {


    @Override
    public ValidationResult apply(ShipClass value, ValueContext context) {
        return check(value, ShipDataVaadinValidatorField.ALL);
    }

    public enum ShipDataVaadinValidatorField {
        ALL,
        NAME,
        MODULES,
        HULL;
    }

    public static ValidationResult check(ShipClass shipClass, ShipDataVaadinValidatorField field) {
        final Map<String, String> errorMessages = new HashMap<>();
        final ErrorLevel status = ErrorLevel.INFO;
        switch (field) {
            case ALL:
                return checkAll(shipClass, status, errorMessages);
            case NAME:
                return checkName(shipClass, status, errorMessages);
            case MODULES:
                return checkModules(shipClass, status, errorMessages);
            case HULL:
                return checkHull(shipClass, status, errorMessages);
        }

        return ValidationResult.error("something went wrong");
    }

    private static ValidationResult checkAll(ShipClass shipClass, ErrorLevel status, Map<String, String> errorMessages) {
        ErrorLevel statusFlag = ErrorLevel.INFO;
        checkName(shipClass, status, errorMessages);
        checkHull(shipClass, status, errorMessages);
        checkModules(shipClass, status, errorMessages);
        return getVaadinStyleResult(errorMessages, statusFlag);
    }

    private static ValidationResult checkHull(ShipClass shipClass, ErrorLevel status, Map<String, String> errorMessages) {
        if (shipClass.getHull() == null) {
            status = ErrorLevel.ERROR;
            errorMessages.put("hull", "Hull must not be empty.");
        }
        return getVaadinStyleResult(errorMessages, status);
    }

    private static ValidationResult checkName(ShipClass shipClass, ErrorLevel status, Map<String, String> errorMessages) {

        String name = shipClass.getName();
        if (StringUtils.isBlank(name)) {
            status = ErrorLevel.ERROR;
            errorMessages.put("name", "Name is empty.");
        } else if (name.length() < 3 || name.length() > 30) {
            status = ErrorLevel.ERROR;
            errorMessages.put("name", "Name is to long or to short.");
        }
        return getVaadinStyleResult(errorMessages, status);
    }

    private static ValidationResult checkModules(ShipClass shipClass, ErrorLevel status, Map<String, String> errorMessages) {
        if (shipClass.getHull() == null) {
            status = ErrorLevel.ERROR;
            errorMessages.put("hull", "Hull must not be empty.");
        } else {
            // check if there is at least the mandatory propulsion module
            final Propulsion propulsion = shipClass.getPropulsion();
            if (propulsion == null) {
                status = ErrorLevel.ERROR;
                errorMessages.put("modules", "Needs at least one item.");
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
                status = ErrorLevel.ERROR;
                errorMessages.put("ConstructionCapacity", "Capacity is overridden.");
            }
        }

        return getVaadinStyleResult(errorMessages, status);
    }


    private static ValidationResult getVaadinStyleResult(Map<String, String> errorMessages, ErrorLevel status) {

        return new ValidationResult() {
            @Override
            public String getErrorMessage() {
                return errorMessages.entrySet().stream().map(stringStringEntry -> {
                    String property = stringStringEntry.getKey();
                    String errorMessage = stringStringEntry.getValue();
                    return property + ": " + errorMessage + "\n";
                }).collect(Collectors.joining());
            }

            @Override
            public Optional<ErrorLevel> getErrorLevel() {
                return Optional.of(status);
            }
        };
    }

    private static int addUsedCapacity(int usedCapacity, @Nullable final BaseModule baseModule) {
        usedCapacity += baseModule != null ? baseModule.getUseCapacity() : 0;
        return usedCapacity;
    }
}
