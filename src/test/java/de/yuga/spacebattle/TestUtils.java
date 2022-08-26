package de.yuga.spacebattle;

import com.google.common.reflect.ClassPath;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Class containing test util methods.
 *
 * @author mschlegel
 */
public final class TestUtils {

    /**
     * Set a field value of an object via reflection.
     *
     * @param object     the object
     * @param fieldName  the field name
     * @param fieldValue the field value
     */
    public static void setFieldValue(@Nonnull final Object object, @Nonnull final String fieldName, @Nonnull final Object fieldValue) {
        Field field = ReflectionUtils.findField(object.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, object, fieldValue);
    }

    /**
     * Generates a random ID and sets it to the entity.
     *
     * @param entity the entity to modify
     */
    public static void setId(@Nonnull AbstractEntityKey entity) {
        final int nextInt = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
        ReflectionTestUtils.setField(entity, "id", nextInt);
    }

    /**
     * Fetches the classes in a specific package.
     *
     * @param packageName the package name
     * @return the found classes
     */
    @SuppressWarnings("UnstableApiUsage")
    public static Set<Class<?>> findAllClassesUsingGoogleGuice(String packageName) throws IOException {
        return ClassPath.from(ClassLoader.getSystemClassLoader())
                .getAllClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName()
                        .equalsIgnoreCase(packageName))
                .map(ClassPath.ClassInfo::load)
                .collect(Collectors.toSet());
    }
}
