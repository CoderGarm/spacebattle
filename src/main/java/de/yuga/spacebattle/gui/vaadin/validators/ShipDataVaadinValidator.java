package de.yuga.spacebattle.gui.vaadin.validators;


import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.validators.ShipDataValidator;

import javax.annotation.Nonnull;
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
        HULL,
        ;
    }

    public static ValidationResult check(ShipClass shipClass, ShipDataVaadinValidatorField field) {
        final Multimap<String, String> errorMap = ArrayListMultimap.create();
        switch (field) {
            case ALL:
                return checkAll(shipClass, errorMap);
            case NAME:
                return checkName(shipClass, errorMap);
            case MODULES:
                return checkModules(shipClass, errorMap);
            case HULL:
                return checkHull(shipClass, errorMap);
        }

        throw new NotifySBUserException("something went wrong while vaadin validation");
    }

    private static ValidationResult checkHull(@Nonnull final ShipClass shipClass, @Nonnull final Multimap<String, String> errorMap) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(errorMap, "errorMap shouldn't be null!");

        ShipDataValidator.checkHull(shipClass, errorMap);
        return getVaadinStyleResult(errorMap);
    }

    private static ValidationResult checkModules(@Nonnull final ShipClass shipClass, @Nonnull final Multimap<String, String> errorMap) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(errorMap, "errorMap shouldn't be null!");

        ShipDataValidator.checkModules(shipClass, errorMap);
        return getVaadinStyleResult(errorMap);
    }

    private static ValidationResult checkName(@Nonnull final ShipClass shipClass, @Nonnull final Multimap<String, String> errorMap) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(errorMap, "errorMap shouldn't be null!");

        ShipDataValidator.checkName(shipClass, errorMap);
        return getVaadinStyleResult(errorMap);
    }

    private static ValidationResult checkAll(@Nonnull final ShipClass shipClass, @Nonnull final Multimap<String, String> errorMap) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(errorMap, "errorMap shouldn't be null!");

        // check if name is valid
        ShipDataValidator.checkName(shipClass, errorMap);
        // check if there is at least the mandatory propulsion module
        ShipDataValidator.checkPropulsion(shipClass, errorMap);
        // check hull
        ShipDataValidator.checkHull(shipClass, errorMap);
        // check if a hull is present
        ShipDataValidator.checkModules(shipClass, errorMap);
        // check predecessor
        ShipDataValidator.checkPredecessor(shipClass, errorMap);

        return getVaadinStyleResult(errorMap);
    }

    private static ValidationResult getVaadinStyleResult(@Nonnull final Multimap<String, String> errorMap) {
        Preconditions.checkNotNull(errorMap, "errorMap shouldn't be null!");

        if (!errorMap.isEmpty()) {
            final String errorMsg = errorMap.entries().stream().map(stringStringEntry -> {
                final String property = stringStringEntry.getKey();
                final String errorMessage = stringStringEntry.getValue();
                return property + ": " + errorMessage + "\n";
            }).collect(Collectors.joining());
            return ValidationResult.error(errorMsg);
        } else {
            return ValidationResult.ok();
        }
    }
}
