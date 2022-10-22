package de.yuga.spacebattle.backend.services.misc;

import com.google.common.base.Preconditions;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DBPatchServiceTest {

    private static final Pattern pattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+-[0-9]+");
    private static final String INSERT_PREFIX = "insert into dbPatch values (null, now()";

    @Test
    void checkDBPatches() {

        final File dir = new File("data/sql/delta/");
        final List<File> subFolders = Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                .filter(f -> f.getName().startsWith("SB"))
                .collect(Collectors.toList());

        final List<File> patchFiles = subFolders.stream()
                .filter(File::isDirectory)
                .map(File::listFiles)
                .filter(Objects::nonNull)
                .map(folder -> Arrays.stream(folder).filter(f -> f.getName().endsWith(".sql")).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        final List<String> patchVersions = patchFiles.stream().map(this::readFile).flatMap(Collection::stream).collect(Collectors.toList());

        assertNotNull(dir);
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
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(INSERT_PREFIX)) {
                    final Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        result.add(matcher.group());
                    }
                }
            }
        }
        return result;
    }
}