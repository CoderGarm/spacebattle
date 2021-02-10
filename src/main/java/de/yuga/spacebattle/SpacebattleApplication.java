package de.yuga.spacebattle;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.vaadin.spring.events.config.EventBusConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@Import(EventBusConfiguration.class)
public class SpacebattleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpacebattleApplication.class, args);
    }

    @Bean
    public static BeanFactoryPostProcessor schemaFilesCleanupPostProcessor() {
        return bf -> {
            try {
                Files.deleteIfExists(Path.of(createPath));
                Files.deleteIfExists(Path.of(dropPath));
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    /**
     * Note: Compare application.properties for path to delete
     */
    private final static String tmpdir = System.getProperty("java.io.tmpdir");
    private final static String createPath = tmpdir + "/createSBDB.sql";
    private final static String dropPath = tmpdir + "/dropSBDB.sql";
}
