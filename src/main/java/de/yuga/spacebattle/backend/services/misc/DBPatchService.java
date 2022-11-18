package de.yuga.spacebattle.backend.services.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.DBPatch;
import de.yuga.spacebattle.backend.repositories.misc.DBPatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DBPatchService {

    private final static Logger LOGGER = LoggerFactory.getLogger(DBPatchService.class);

    private static final Pattern pattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+-[0-9]+");
    private static final String INSERT_PREFIX = "insert into dbPatch values (null, now()";

    @Nonnull
    private final DBPatchRepository dbPatchRepository;

    @Nonnull
    private final String dbPatchFolder;

    public DBPatchService(@Nonnull @Value("${db-patch-folder}") final String dbPatchFolder,
                          @Nonnull final DBPatchRepository dbPatchRepository) {
        this.dbPatchRepository = Preconditions.checkNotNull(dbPatchRepository, "dbPatchRepository must not be empty");
        this.dbPatchFolder = Preconditions.checkNotNull(dbPatchFolder, "dbPatchFolder must not be empty");
    }

    /**
     * Checks if every existing db patch is applied.
     *
     * @return <code>true</code> if every code is applied, <code>false</code> otherwise
     */
    public boolean checkDBPatches() {
        final List<String> dbPatchVersions = fetchDBPatchVersions();
        final List<DBPatch> appliedPatches = Objects.requireNonNullElse(dbPatchRepository.findAll(), new ArrayList<>());
        final Set<String> versionStrings = appliedPatches.stream()
                .map(DBPatch::getVersion)
                .collect(Collectors.toSet());
        dbPatchVersions.removeAll(versionStrings);
        return dbPatchVersions.isEmpty();
    }


    private List<String> fetchDBPatchVersions() {
        final File dir = new File(dbPatchFolder);
        final File[] files = dir.listFiles();
        final List<File> subFolders = Arrays.stream(files)
                .filter(f -> f.getName().startsWith("SB"))
                .collect(Collectors.toList());

        final List<File> patchFiles = subFolders.stream()
                .filter(File::isDirectory)
                .map(File::listFiles)
                .filter(Objects::nonNull)
                .map(folder -> Arrays.stream(folder).filter(f -> f.getName().endsWith(".sql")).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return patchFiles.stream().map(this::readFile).flatMap(Collection::stream).collect(Collectors.toList());
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
