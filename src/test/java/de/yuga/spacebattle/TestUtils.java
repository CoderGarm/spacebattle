package de.yuga.spacebattle;

import com.google.common.base.Preconditions;
import com.google.common.reflect.ClassPath;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.FieldName;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.WikiShipClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

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

    @Nonnull
    public static List<WikiShipClass> readShipClasses(final String dir, final String folderName) {
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(FieldName.class).setSkipHeaderRecord(true).build();

        final List<WikiShipClass> result = new ArrayList<>();
        try {
            final File folder = new File(dir + folderName);
            if (!folder.exists() || !folder.isDirectory()) {
                fail("Folder did not exist.");
            }

            for (final File file : Objects.requireNonNull(folder.listFiles())) {
                if (file.getName().startsWith(".")) {
                    continue;
                }
                final Map<FieldName, List<String>> keyValues = new HashMap<>();
                final Reader in = new FileReader(file);
                final Iterable<CSVRecord> records = csvFormat.parse(in);
                for (final CSVRecord record : records) {
                    for (final FieldName fieldName : FieldName.values()) {
                        final String value = record.get(fieldName);
                        final List<String> values = keyValues.getOrDefault(fieldName, new ArrayList<>());
                        if (StringUtils.isNotEmpty(value)) {
                            values.add(StringUtils.isNotEmpty(value) ? value : "");
                        }
                        keyValues.put(fieldName, values);
                    }
                }
                final WikiShipClass wikiShipClass = new WikiShipClass(keyValues);
                result.add(wikiShipClass);
            }
            return result;
        } catch (final Exception e) {
            fail(e);
        }
        return result;
    }

    public static void writeShipClass(final String dir, final String fileName, final WikiShipClass wikiShipClass) {

        final StringWriter sw = new StringWriter();
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(FieldName.class).build();
        try {

            try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
                final List<List<String>> csvRows = wikiShipClass.getCsvRows();
                for (final List<String> csvRow : csvRows) {
                    printer.printRecord(csvRow);
                }
                final String text = sw.toString().trim();
                writeString(dir, fileName, text);
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void writeString(final String dir, final String fileName, final String string) {
        try {
            new File(dir).mkdirs();
            final File file = new File(dir + fileName.replaceAll("\\s", "-"));
            if (file.exists()) {
                file.delete();
            } else {
                file.createNewFile();
            }
            final FileOutputStream outputStream = new FileOutputStream(dir + fileName.replaceAll("\\s", "-"));
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
