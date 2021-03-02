package de.yuga.spacebattle.backend.validators;


import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.enums.EModuleType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
public class ShipDataValidator implements ConstraintValidator<ShipValidator, ShipClass> {

    public void initialize(ShipValidator constraint) {
    }

    public boolean isValid(ShipClass shipClass, ConstraintValidatorContext context) {

        boolean isError = false;

        String name = shipClass.getName();
        if (StringUtils.isBlank(name)) {
            isError = true;
            setConstraintViolation("name", "Name is empty.", context);
        } else if (name.length() < 3 || name.length() > 30) {
            isError = true;
            setConstraintViolation("name", "Name is to long or to short.", context);
        }

        if (shipClass.getHull() == null) {
            isError = true;
            setConstraintViolation("hull", "Hull must not be empty.", context);
        }

        final Map<Module, Integer> modulesMap = shipClass.getModules();

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
            // checks is capacity is overridden
            final Integer usedCapacity = modules.stream().map(Module::getUseCapacity).reduce(0, Integer::sum);
            int constructionCapacity = shipClass.getHull().getConstructionCapacity();
            if (usedCapacity > constructionCapacity) {
                setConstraintViolation("ConstructionCapacity", "Capacity is overridden.", context);
                isError = true;
            }
        }
        return !isError;
    }

    private static void setConstraintViolation(String property, String msg, ConstraintValidatorContext context) {
        context.buildConstraintViolationWithTemplate(msg).addPropertyNode(property).addConstraintViolation();
    }
}
