package de.yuga.spacebattle.validators.base;

import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public class MyMessageInterpolator implements MessageInterpolator {
    private final MessageInterpolator defaultInterpolator;

    public MyMessageInterpolator(MessageInterpolator interpolator) {
        this.defaultInterpolator = interpolator;
    }

    @Override
    public String interpolate(String messageTemplate, Context context) {
        messageTemplate = messageTemplate.toUpperCase();
        return defaultInterpolator.interpolate(messageTemplate, context);
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        messageTemplate = messageTemplate.toUpperCase();
        return defaultInterpolator.interpolate(messageTemplate, context, locale);
    }

    public static <T> void printConstraintViolations(Logger LOGGER, Set<ConstraintViolation<T>> set) {

        for (ConstraintViolation<T> cv : set) {
            LOGGER.info("Property: '" + cv.getPropertyPath() + "' Message: '" + cv.getMessage() + "'");
        }


    }


}
