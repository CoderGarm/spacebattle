package de.yuga.spacebattle.backend.services.caches.file;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CacheFileWriter {

    @Nonnull
    private static final String USR_HOME = System.getProperty("user.home");

    @Nonnull
    private static final String PS = System.getProperty("file.separator");

    @Nonnull
    private static final String KEY_SEPARATOR = "<\\|>";

    @Nonnull
    private static final String KEY_SEPARATOR_WRITER = "<|>";

    @Nonnull
    private final String cacheDir;

    @Autowired
    public CacheFileWriter(@Nonnull @Value("${cache-dir:file-cache}") final String cacheDir) {
        this.cacheDir = Preconditions.checkNotNull(cacheDir, "cacheDir shouldn't be null!");
    }


    private BufferedWriter openStream(@Nonnull final String fileName) {
        Preconditions.checkNotNull(fileName, "fileName must not be empty");

        try {
            final String path = USR_HOME + PS + cacheDir;
            final File dir = new File(path);
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            final FileWriter fw = new FileWriter(path + PS + fileName, true);
            return new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
            throw new NotifyWebUserException("CacheFileWriter receives an error on open" + e.getMessage());
        }
    }

    private void write(@Nonnull final BufferedWriter bw, final String msg) {
        Preconditions.checkNotNull(bw, "bw must not be empty");

        try {
            bw.write(msg);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            throw new NotifyWebUserException("CacheFileWriter receives an error on write" + e.getMessage());
        }
    }

    private void closeAndWrite(@Nonnull final BufferedWriter bw) {
        Preconditions.checkNotNull(bw, "bw must not be empty");
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new NotifyWebUserException("CacheFileWriter receives an error on close " + e.getMessage());
        }
    }

    public void writeToFile(@Nonnull final Class<?> aClass, @Nonnull final String key, @Nonnull final String value) {
        Preconditions.checkNotNull(aClass, "aClass must not be empty");
        Preconditions.checkNotNull(key, "key must not be empty");
        Preconditions.checkNotNull(value, "value must not be empty");

        final BufferedWriter bufferedWriter = openStream(aClass.getSimpleName());

        write(bufferedWriter, key + KEY_SEPARATOR_WRITER + value);

        closeAndWrite(bufferedWriter);
    }

    public Map<String, List<String>> getFileCacheContent(@Nonnull final Class<?> aClass) {
        Preconditions.checkNotNull(aClass, "aClass must not be empty");

        final String path = USR_HOME + PS + cacheDir + PS + aClass.getSimpleName();
        final File dir = new File(path);
        final List<String> strings = readFile(dir);
        final Map<String, List<String>> result = new HashMap<>();

        strings.forEach(string -> {
            final String[] split = string.split(KEY_SEPARATOR);
            final String key = split[0];
            final List<String> list = result.getOrDefault(key, new ArrayList<>());
            list.add(split[1]);
            result.put(key, list);
        });

        return result;
    }

    @Nonnull
    private List<String> readFile(@Nonnull final File file) {
        Preconditions.checkNotNull(file, "file must not be empty");

        if (!file.exists()) {
            return new ArrayList<>();
        }

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
}
