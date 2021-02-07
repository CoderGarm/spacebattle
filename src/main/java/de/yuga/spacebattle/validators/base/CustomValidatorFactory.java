package de.yuga.spacebattle.validators.base;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class CustomValidatorFactory {


    public static Validator buildCustomValidator() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        /*
        Validator validator = factory.usingContext()
                .messageInterpolator( new MyMessageInterpolator(Validation.byDefaultProvider().configure().getDefaultMessageInterpolator()) )
                .getValidator();
        */
        Validator validator = factory.getValidator();

        return validator;
    }

}
