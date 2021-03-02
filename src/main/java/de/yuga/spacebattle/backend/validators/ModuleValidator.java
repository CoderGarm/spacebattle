package de.yuga.spacebattle.backend.validators;

import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.enums.EModuleType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleValidator implements ConstraintValidator<ModuleChecker, Map<Module, Integer>> {
    public void initialize(ModuleChecker constraint) {
    }

    public boolean isValid(Map<Module, Integer> modulesMap, ConstraintValidatorContext context) {
        boolean isError = false;
        final Set<Module> modules = modulesMap.keySet();
        if (modules.isEmpty()) {
            setConstraintViolation("modules", "Needs at least one module for mandatory module classes", context);
            isError = true;
        } else {
            // checks if every mandatory module class is existent
            final List<EModuleType> mandatory = EModuleType.getMandatories();
            final List<EModuleType> listOfModuleTypesFromShip = modules.stream().map(Module::getModuleType).collect(Collectors.toList());
            final List<EModuleType> notFulfilledMandatory = mandatory.stream().filter(mand -> !listOfModuleTypesFromShip.contains(mand)).collect(Collectors.toList());

            if (!notFulfilledMandatory.isEmpty()) {
                for (EModuleType m : notFulfilledMandatory) {
                    setConstraintViolation(m.getName(), "Needs at least one item.", context);
                }
                isError = true;
            }
        }
        return !isError;
    }

    private static void setConstraintViolation(String property, String msg, ConstraintValidatorContext context) {
        context.buildConstraintViolationWithTemplate(msg).addPropertyNode(property).addConstraintViolation();
    }
}
