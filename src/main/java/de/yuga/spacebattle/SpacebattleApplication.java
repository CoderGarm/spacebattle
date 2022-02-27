package de.yuga.spacebattle;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.http.MediaType;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@EnableJpaRepositories(queryLookupStrategy = QueryLookupStrategy.Key.USE_DECLARED_QUERY)
@Import({BeanValidatorPluginsConfiguration.class})
public class SpacebattleApplication {

    @Autowired
    private TypeResolver typeResolver;

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

    private ApiKey apiKey() {
        return new ApiKey("JWT", "Authorization", "header");
    }

    /**
     * Creates a complete endpoint documentation for swagger.
     *
     * @return a docket
     */
    @Bean
    public Docket api() {
        final ApiInfo apiInfo = new ApiInfo("BoF REST API",
                //new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(CHANGELOG_FILENAME))).lines().collect(Collectors.joining(System.lineSeparator())),
                "description",
                "0.0.1", "",
                new Contact("team", "", "bla@bla.com"),
                "Apache 2",
                "https://www.apache.org/licenses/LICENSE-2.0",
                Collections.emptyList());

        final List<Class<?>> jsonDtoClasses = findTransferObjects();
        final ResolvedType firstResolvedType = typeResolver.resolve(jsonDtoClasses.get(0));
        jsonDtoClasses.remove(jsonDtoClasses.get(0));
        jsonDtoClasses.add(Distance.class);
        jsonDtoClasses.add(Acceleration.class);
        final List<ResolvedType> resolvedTypes = jsonDtoClasses.stream().map(e -> typeResolver.resolve(e)).collect(Collectors.toList());
        final ResolvedType[] remainingResolvedTypes = resolvedTypes.toArray(new ResolvedType[]{});
        return new Docket(DocumentationType.OAS_30)
                .select()
                //.apis(RequestHandlerSelectors.any()) // todo disable or secure actuator for productive - commented out to exclude actuator endpoints
                .apis(RequestHandlerSelectors.basePackage("de.yuga.spacebattle.rest.api"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo)
                .securitySchemes(Collections.singletonList(apiKey()))
                .produces(Set.of(MediaType.APPLICATION_JSON_VALUE))
                .consumes(Set.of(MediaType.APPLICATION_JSON_VALUE))
                .forCodeGeneration(true)
                .protocols(Set.of("http"))
                .host("localhost:8080")
                .useDefaultResponseMessages(false)
                .additionalModels(firstResolvedType, remainingResolvedTypes)
                ;
    }

    private List<Class<?>> findTransferObjects() {
        List<Class<?>> candidates = new ArrayList<>();
        try {
            String basePackage = "de.yuga.spacebattle.rest.dto";
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

            String resourcePath = ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resourcePath + "/" + "**/*.class";
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                }
            }
        } catch (final IOException | ClassNotFoundException ignored) {
        }
        return candidates;
    }
}
