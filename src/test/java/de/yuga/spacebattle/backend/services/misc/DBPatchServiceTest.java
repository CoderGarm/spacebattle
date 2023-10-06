package de.yuga.spacebattle.backend.services.misc;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DBPatchServiceTest {

    private static final String INSERT_PREFIX = "insert into dbPatch values (null, now()";
    private static final Pattern DB_PATCH_PATTERN = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+-[0-9]+");

    private static final String CREATE_PREFIX = "create table ";
    private static final String STATEMENT_SUFFIX = ";";

    @Test
    void checkDBPatchesForInsertStatement() {

        final List<File> patchFiles = getPatchFiles(false);
        final List<List<String>> patchContentByFile = patchFiles.stream().map(this::readFile)
                .collect(Collectors.toList());
        patchContentByFile.forEach(this::validatePatchVersion);
    }

    @Nonnull
    private List<File> getPatchFiles(final boolean onlyLatestSeason) {
        final File dir = new File("data/sql/delta/");
        assertNotNull(dir);
        final Set<File> seasonFolders = Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                .filter(File::isDirectory)
                .map(File::listFiles)
                .filter(Objects::nonNull)
                .map(files -> Arrays.stream(files).collect(Collectors.toSet()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        if (onlyLatestSeason) {
            final List<File> files = seasonFolders.stream().sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
            final File latest = files.get(files.size() - 1);
            seasonFolders.clear();
            seasonFolders.add(latest);
        }

        final List<File> subFolders = seasonFolders.stream()
                .filter(f -> f.getName().startsWith("SB"))
                .collect(Collectors.toList());

        return subFolders.stream()
                .filter(File::isDirectory)
                .map(File::listFiles)
                .filter(Objects::nonNull)
                .map(folder -> Arrays.stream(folder).filter(f -> f.getName().endsWith(".sql")).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Test
    void checkCreateFile() {
        final File file = new File("data/sql/createSBDB.sql");
        final List<String> lines = readFile(file);

        final List<String> patchLinesFromCreate = lines.stream().filter(line -> line.startsWith(INSERT_PREFIX)).collect(Collectors.toList());

        final List<File> patchFiles = getPatchFiles(true);
        final List<String> linesInPatches = patchFiles.stream().map(this::readFile)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        final List<String> versionLinesFromPatches = linesInPatches.stream().filter(line -> line.startsWith(INSERT_PREFIX)).collect(Collectors.toList());

        versionLinesFromPatches.removeAll(patchLinesFromCreate);
        if (!versionLinesFromPatches.isEmpty()) {
            System.out.println("Missing patches in " + file.getName());
            versionLinesFromPatches.forEach(System.out::println);
            fail("Missing patches in " + file.getName());
        }
    }

    @Test
    void checkNamedCheckConstraints() {
        final List<String> invalidCheckConstraints = new ArrayList<>();

        final File file = new File("data/sql/createSBDB.sql");
        List<String> lines = readFile(file);
        lines = lines.stream().map(String::trim).collect(Collectors.toList());

        final String content = String.join("", lines);
        final int count = StringUtils.countMatches(content, CREATE_PREFIX);

        final List<String> statements = Arrays.stream(content.split(STATEMENT_SUFFIX)).map(statement -> statement + ";").collect(Collectors.toList());
        final List<String> createStatements = new ArrayList<>();
        for (final String statement : statements) {
            if (statement.startsWith(CREATE_PREFIX)) {
                createStatements.add(statement);
            }
        }
        assertEquals(count, createStatements.size());

        for (final String createStatement : createStatements) {
            assertEquals(1, (int) Arrays.stream(createStatement.split(STATEMENT_SUFFIX)).filter(s -> !s.isBlank()).count());
            assertEquals(1, (int) Arrays.stream(createStatement.split(CREATE_PREFIX)).filter(s -> !s.isBlank()).count());
        }

        for (final String createTableStatement : createStatements) {
            if (createTableStatement.contains("check")) {
                final int checkIndex = createTableStatement.indexOf("check");
                int indexOfComma = 0;
                for (int i = checkIndex; i >= 0; i--) {
                    final char c = createTableStatement.charAt(i);
                    if (c == ',') {
                        indexOfComma = i + 1;
                        break;
                    }
                }
                final String areaOfConstraintName = createTableStatement.substring(indexOfComma, checkIndex);
                if (!areaOfConstraintName.contains("constraint")) {
                    final String tableName = createTableStatement.split("\\(")[0].replace(CREATE_PREFIX, "").trim();
                    invalidCheckConstraints.add(tableName);
                }

            }
        }
        if (!invalidCheckConstraints.isEmpty()) {
            System.out.println("Replace unnamed check constraints for the following tables.");
            invalidCheckConstraints.forEach(this::printMissingConstraint);
            fail("There are missing constraint names for check constraints.");
        }

    }

    private void printMissingConstraint(@Nonnull final String tableName) {
        Preconditions.checkNotNull(tableName, "tableName must not be empty");

        System.out.println("constraint " + tableName.toUpperCase() + "_CHECK check (");
    }

    @Nonnull
    private List<String> readFile(@Nonnull final File file) {
        Preconditions.checkNotNull(file, "file must not be empty");

        try (final InputStream inputStream = Files.newInputStream(file.toPath())) {
            return readFromInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError(e);
        }
    }


    @Nonnull
    private List<String> readFromInputStream(@Nonnull final InputStream inputStream) throws IOException {
        Preconditions.checkNotNull(inputStream, "inputStream must not be empty");

        final List<String> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        }
        return result;
    }

    private void validatePatchVersion(@Nonnull final List<String> contentLines) {
        Preconditions.checkNotNull(contentLines, "contentLines must not be empty");

        boolean patchHasVersion = false;
        for (final String line : contentLines) {
            if (line.toLowerCase().startsWith(INSERT_PREFIX.toLowerCase())) {
                final Matcher matcher = DB_PATCH_PATTERN.matcher(line);
                if (matcher.find()) {
                    patchHasVersion = true;
                }
            }
        }
        if (!patchHasVersion) {
            fail("There are database patches that didn't have a version inside. Please correct it.");
        }
    }
}
