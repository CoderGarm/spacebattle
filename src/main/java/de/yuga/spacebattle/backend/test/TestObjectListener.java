package de.yuga.spacebattle.backend.test;

import com.google.common.base.Preconditions;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Das klappt leider nicht - ich wollte den gemockten Konstructor gewissermaßen statt der after method
 * erneut durchführen und das object zuweisen.
 */
public class TestObjectListener extends TestListenerAdapter {

    /**
     * The name of every tested object.
     */
    public static final String TEST_OBJECT_NAME = "testObject";

    /**
     * Annotation for test objects that must be resetted after each {@link Test}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestObject {
    }

    /**
     * Will reset the testObject before each test.
     */
    @Override
    public void onTestStart(@Nonnull final ITestResult result) {
        Preconditions.checkNotNull(result, "result must not be null.");


        Object testObjectToReset = getAnnotatedField(result);

    }

    private Object getAnnotatedField(ITestResult result) {
        for (final Field field : result.getTestClass().getRealClass().getDeclaredFields()) {

            final Annotation annotation = Arrays.stream(field.getAnnotations())
                    .filter(a -> a.annotationType().equals(TestObject.class))
                    .findAny().orElse(null);

            if (annotation == null) {
                continue;
            }

            if (TEST_OBJECT_NAME.equals(field.getName())) {
                return field;
            } else {
                throw new IllegalArgumentException("You should name this field '" + TEST_OBJECT_NAME + "' or do not annotate it.");
            }
        }
        throw new IllegalArgumentException("You should name at least the annotated field '" + TEST_OBJECT_NAME + "' or do not annotate it.");
    }
}
