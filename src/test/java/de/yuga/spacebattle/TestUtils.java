package de.yuga.spacebattle;

import com.google.common.base.Preconditions;
import com.google.common.reflect.ClassPath;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
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
    public static void setId(@Nonnull final AbstractEntityKey entity) {
        Preconditions.checkNotNull(entity, "entity must not be empty");

        final int nextInt = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
        ReflectionTestUtils.setField(entity, "id", nextInt);
    }


    public static void setId(@Nonnull final AbstractEntityKey entity, final int i) {
        Preconditions.checkNotNull(entity, "entity must not be empty");

        ReflectionTestUtils.setField(entity, "id", i);
    }

    /**
     * Fetches the classes in a specific package.
     *
     * @param packageName the package name
     * @return the found classes
     */
    @SuppressWarnings("UnstableApiUsage")
    public static Set<Class<?>> findAllClassesInPackage(String packageName) {
        try {
            return ClassPath.from(ClassLoader.getSystemClassLoader())
                    .getAllClasses()
                    .stream()
                    .filter(clazz -> clazz.getPackageName()
                            .equalsIgnoreCase(packageName))
                    .map(ClassPath.ClassInfo::load)
                    .collect(Collectors.toSet());
        } catch (final IOException e) {
            e.printStackTrace();
            return Set.of();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Set<Class<?>> findAllClassesInPackageRecursive(String packageName) {
        try {
            return ClassPath.from(ClassLoader.getSystemClassLoader())
                    .getAllClasses()
                    .stream()
                    .filter(clazz -> clazz.getPackageName().toLowerCase()
                            .startsWith(packageName.toLowerCase()))
                    .map(ClassPath.ClassInfo::load)
                    .collect(Collectors.toSet());
        } catch (final IOException e) {
            e.printStackTrace();
            return Set.of();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void writeString(final String dir, final String fileName, final String string) {
        try {
            new File(dir).mkdirs();
            final File file = new File(dir + fileName);
            if (file.exists()) {
                file.delete();
            } else {
                file.createNewFile();
            }
            final FileOutputStream outputStream = new FileOutputStream(dir + fileName);
            byte[] strToBytes = string.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static String readFile(final String filename) {
        final File file = new File(filename);
        try (final InputStream inputStream = Files.newInputStream(file.toPath())) {
            return readFromInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError(e);
        }
    }

    public static String readFile(final File file) {
        try (final InputStream inputStream = Files.newInputStream(file.toPath())) {
            return readFromInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError(e);
        }
    }
}
