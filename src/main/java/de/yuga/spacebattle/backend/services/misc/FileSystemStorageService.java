package de.yuga.spacebattle.backend.services.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Objects;

@Service
public class FileSystemStorageService {

    @Nonnull
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FileSystemStorageService.class);

    @Nonnull
    private static final String USR_HOME = System.getProperty("user.home");

    @Nonnull
    private static final String PS = FileSystems.getDefault().getSeparator();

    @Nonnull
    private final Path cacheDir;

    @Autowired
    public FileSystemStorageService(@Nonnull @Value("${sb.cache-dir:file-cache}") final String cacheDir) {
        this.cacheDir = Path.of(USR_HOME + PS
                + Preconditions.checkNotNull(cacheDir, "cacheDir shouldn't be null!")
                + PS + "images" + PS);
    }

    public void store(@Nonnull final MultipartFile file, @Nonnull final String fileName) {
        Preconditions.checkNotNull(file, "file must not be empty");
        Preconditions.checkNotNull(fileName, "fileName must not be empty");

        try {
            if (file.isEmpty()) {
                throw new NotifyWebUserException("Failed to store empty file.");
            }
            final Path destinationFile = getDestinationFile(file, fileName);
            if (!destinationFile.getParent().equals(cacheDir.toAbsolutePath())) {
                // This is a security check
                throw new NotifyWebUserException("Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                final File dir = new File(cacheDir.toUri());
                if (!dir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdirs();
                }

                long size = FileUtils.sizeOfDirectory(cacheDir.toFile());
                if (size > 500000000L) {
                    LOGGER.info("File '{}' not written due folder size > 500 MB", destinationFile);
                } else {
                    Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new NotifyWebUserException("Failed to store file.");
        }
    }

    @Nonnull
    private Path getDestinationFile(@Nonnull final MultipartFile file, @Nonnull final String fileName) {
        Preconditions.checkNotNull(file, "file must not be empty");
        Preconditions.checkNotNull(fileName, "fileName must not be empty");
        Preconditions.checkNotNull(file.getOriginalFilename(), "file.getOriginalFilename() must not be empty");

        final String originalFilename = file.getOriginalFilename();
        final String fileExtension = originalFilename.contains(".") ? originalFilename.split("\\.")[1] : "";
        return cacheDir.resolve(Paths.get(fileName + "." + fileExtension)).normalize().toAbsolutePath();
    }

    @Nullable
    public Path loadFreshestFile(@Nonnull final String filename) {
        Preconditions.checkNotNull(filename, "filename must not be empty");

        final File[] array = Objects.requireNonNullElse(new File(cacheDir.toUri()).listFiles(), new File[]{});
        return Arrays.stream(array)
                .filter(file -> file.getName().startsWith(filename))
                .reduce((file, file2) -> getCreationDate(file.toPath()).compareTo(getCreationDate(file2.toPath())) < 0 ? file : file2)
                .map(File::toPath)
                .orElse(null);
    }

    @Nullable
    public File loadAsResource(String filename) {
        try {
            final Path file = loadFreshestFile(filename);
            if (file == null) {
                return null;
            }
            final Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource.getFile();
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    @Nonnull
    public FileTime getCreationDate(@Nonnull final Path path) {
        try {
            final BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            return attr.creationTime();
        } catch (IOException ex) {
            return FileTime.fromMillis(0);
        }
    }
}
