package de.yuga.spacebattle;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.misc.DBPatchService;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@EnableScheduling
@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@EnableJpaRepositories(queryLookupStrategy = QueryLookupStrategy.Key.USE_DECLARED_QUERY)
public class SpacebattleApplication implements Jackson2ObjectMapperBuilderCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpacebattleApplication.class);

    /**
     * Note: Compare application.properties for path to delete
     */
    private static final String tmpdir = System.getProperty("java.io.tmpdir");
    private static final String separator = System.getProperty("file.separator");
    private static final String createPath = tmpdir + separator + "createSBDB.sql";
    private static final String dropPath = tmpdir + separator + "dropSBDB.sql";

    @Nonnull
    private final String applicationVersion;

    @Nonnull
    private final DBPatchService dbPatchService;

    @Nonnull
    private final ApplicationContext context;

    @Autowired
    public SpacebattleApplication(@Nonnull @Value("${sb.version:nope}") final String version,
                                  @Nonnull final DBPatchService dbPatchService,
                                  @Nonnull final ApplicationContext context) {
        this.applicationVersion = Preconditions.checkNotNull(version, "version must not be empty");
        this.dbPatchService = Preconditions.checkNotNull(dbPatchService, "dbPatchService must not be empty");
        this.context = Preconditions.checkNotNull(context, "context must not be empty");
    }

    public static void main(String[] args) {
        SpringApplication.run(SpacebattleApplication.class, args);
    }

    /**
     * Stops the application in case of a missing database patch.
     */
    @PostConstruct
    public void validateDBPatches() {
        final boolean everyPatchPresent = dbPatchService.checkDBPatches();
        if (!everyPatchPresent) {
            throw new ManualShutdownException("Not all db patches are applied.", "Please check the databases patches which have to be applied.");
        } else {
            LOGGER.info("All database patches applied.");
        }
    }

    @EventListener
    public void onShutdown(ContextStoppedEvent event) {
        LOGGER.info("yeah");
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
     * Creates a complete endpoint documentation for swagger.
     *
     * @return a docket
     */
    @Bean
    public OpenAPI spacebattleOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("BoF REST API")
                        .description("Battle for honor interface")
                        .contact(new Contact().email("webmaster@batleforhonor.de"))
                        .version(this.applicationVersion)
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addServersItem(new Server().description("dev").url("http://localhost:8080"))
                .addServersItem(new Server().description("qat").url("http://65.109.16.233:8081"))
                .addServersItem(new Server().description("prod").url("https://www.battleforhonor.de"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.APIKEY)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .components(new Components()
                        .addHeaders("few", new Header().content(new Content().addMediaType("media type", new MediaType())))
                )
                ;
    }

    @Override
    public void customize(final Jackson2ObjectMapperBuilder builder) {
        final LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        builder.failOnEmptyBeans(false)
                .serializerByType(LocalDateTime.class, new ToStringSerializer())
                .deserializerByType(LocalDateTime.class, localDateTimeDeserializer);
    }
}
