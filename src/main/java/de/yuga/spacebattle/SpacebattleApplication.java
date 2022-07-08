package de.yuga.spacebattle;

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
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@EnableJpaRepositories(queryLookupStrategy = QueryLookupStrategy.Key.USE_DECLARED_QUERY)
public class SpacebattleApplication {

    /**
     * Note: Compare application.properties for path to delete
     */
    private final static String tmpdir = System.getProperty("java.io.tmpdir");
    private final static String separator = System.getProperty("file.separator");
    private final static String createPath = tmpdir + separator + "createSBDB.sql";
    private final static String dropPath = tmpdir + separator + "dropSBDB.sql";

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
                        .contact(new Contact().email("bla@bla.com"))
                        .version("0.0.1")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addServersItem(new Server().description("dev").url("http://localhost:8080"))
                .addServersItem(new Server().description("qat").url("http://65.109.16.233:8081"))
                /*.externalDocs(new ExternalDocumentation()
                        .description("SpringShop Wiki Documentation")
                        .url("https://springshop.wiki.github.org/docs"))*/
                /*
                 */
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
}
