package de.yuga.spacebattle.gui.vaadin.validators;


import com.vaadin.flow.data.binder.ErrorLevel;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.enums.EModuleType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
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
        final Map<Module, Integer> modulesMap = shipClass.getModules();
        final Set<Module> modules = modulesMap.keySet();
        if (modules.isEmpty()) {
            status = ErrorLevel.ERROR;
            errorMessages.put("modules", "Needs at least one module for mandatory module classes");
        } else if (shipClass.getHull() == null) {
            status = ErrorLevel.ERROR;
            errorMessages.put("hull", "Hull must not be empty.");
        } else {
            // checks if every mandatory module class is existent
            final List<EModuleType> mandatory = EModuleType.getMandatories();
            final List<EModuleType> listOfModuleTypesFromShip = modules.stream().map(Module::getModuleType).collect(Collectors.toList());
            final List<EModuleType> notFulfilledMandatory = mandatory.stream().filter(mand -> !listOfModuleTypesFromShip.contains(mand)).collect(Collectors.toList());

            if (!notFulfilledMandatory.isEmpty()) {
                status = ErrorLevel.ERROR;
                for (EModuleType m : notFulfilledMandatory) {
                    errorMessages.put(m.getName(), "Needs at least one item.");
                }
            }
            // checks is capacity is overridden
            final Integer usedCapacity = modules.stream().map(Module::getUseCapacity).reduce(0, Integer::sum);
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
}
