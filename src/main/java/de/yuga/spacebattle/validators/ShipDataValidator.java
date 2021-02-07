package de.yuga.spacebattle.validators;


import de.yuga.spacebattle.entities.constructables.spacecrafts.ShipClass;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ShipDataValidator implements ConstraintValidator<ShipValidator, ShipClass> {


    public void initialize(ShipValidator constraint) {
    }

    public boolean isValid(ShipClass obj, ConstraintValidatorContext context) {

        boolean isError = false;
/*
        Set<Module> modules = obj.getModules();

        if (modules == null || modules.isEmpty()) {

            setConstraintViolation("modules", "Needs at least one module for mandatory module classes", context);
            isError = true;
        } else {

            // checks if every mandatory module class is existent
            List<EModuleType> mandatories = EModuleType.getMandatories();
            List<EModuleType> listOfModuleTypesFromShip = modules.stream().map(Module::getModuleType).collect(Collectors.toList());
            List<EModuleType> notFulfilledMandatories = mandatories.stream().filter(mand -> !listOfModuleTypesFromShip.contains(mand)).collect(Collectors.toList());

            if (!notFulfilledMandatories.isEmpty()) {

                for (EModuleType m : notFulfilledMandatories) {
                    setConstraintViolation(m.getName(), "Needs at least one item.", context);
                }
                isError = true;
            }

            // checks is capacity is overridden
            Integer usedCapacity = modules.stream().map(Module::getUseCapacity).reduce(0, Integer::sum);
            int constructionCapacity = obj.getConstructionCapacity();
            if (usedCapacity > constructionCapacity) {
                setConstraintViolation("ConstructionCapacity", "Capacity is overridden.", context);
                isError = true;
            }

        }
*/

        return !isError;
    }

    private static void setConstraintViolation(String property, String msg, ConstraintValidatorContext context) {
        context.buildConstraintViolationWithTemplate(msg).addPropertyNode(property).addConstraintViolation();
    }
}
