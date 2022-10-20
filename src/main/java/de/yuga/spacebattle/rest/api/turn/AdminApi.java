package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.i18n.Translatable;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.i18n.TranslatableService;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.backend.transformer.BuildingCsvTransformer;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.ApplicationInfo;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.misc.FileUpload;
import de.yuga.spacebattle.rest.dto.turn.Tick;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.ADMIN_BASE_ENDPOINT;

@Tag(name = "AdminApi")
@RolesAllowed("ADMIN")
@RestController
@RequestMapping(value = "/" + ADMIN_BASE_ENDPOINT + "/" + AdminApi.ENDPOINT + "/")
public class AdminApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "admin";

    private final static Logger LOGGER = LoggerFactory.getLogger(AdminApi.class);

    @Nonnull
    private final TickService tickController;

    @Nonnull
    private final TranslatableService translatableService;

    @Nonnull
    private final BuildingService buildingService;

    @Nonnull
    private final String applicationVersion;

    @Autowired
    public AdminApi(@Nonnull final TickService tickController,
                    @Nonnull final TranslatableService translatableService,
                    @Nonnull final BuildingService buildingService,
                    @Nonnull @Value("${sb.version:nope}") final String version) {
        this.tickController = Preconditions.checkNotNull(tickController, "tickC shouldn't be null!");
        this.translatableService = Preconditions.checkNotNull(translatableService, "translatableService must not be empty");
        this.applicationVersion = Preconditions.checkNotNull(version, "version must not be empty");
        this.buildingService = Preconditions.checkNotNull(buildingService, "buildingService must not be empty");
    }

    @GetMapping(value = "/version")
    @Operation(summary = "Get the current application version.", operationId = "getVersion",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApplicationInfo.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getVersion() {
        return ResponseEntity.ok(new ApplicationInfo(applicationVersion));
    }

    @GetMapping(value = "/doTick")
    @Operation(summary = "Get the current tick.", operationId = "doTick",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Tick.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> doTick() {
        LOGGER.info("Tick initialized");
        final de.yuga.spacebattle.backend.entities.turn.Tick now = tickController.doTick();
        return ResponseEntity.ok(new Tick(now));
    }

    @GetMapping(value = "/languages")
    @Operation(summary = "Get the current tick.", operationId = "getPossibleLanguages",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = String.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPossibleLanguages() {
        return ResponseEntity.ok(Translation.LOCALES.stream().map(Locale::getLanguage).collect(Collectors.toSet()));
    }

    @GetMapping(value = "/translations")
    @Operation(summary = "Get the current tick.", operationId = "getTranslations",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.i18n.Translation.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getTranslations() {
        final List<Translatable> translatables = translatableService.findAll();
        final Map<Integer, Translatable> translatablesById = translatables.stream().collect(Collectors.toMap(Translatable::getId, Function.identity()));
        final Map<Integer, Set<Translation>> translationsById = translatables.stream().collect(Collectors.toMap(AbstractEntityKey::getId, Translatable::getTranslations));
        final List<de.yuga.spacebattle.rest.dto.i18n.Translation> translations = translationsById.entrySet().stream().map(e -> {
            final Integer idTranslatable = e.getKey();
            final Translatable translatable = translatablesById.get(idTranslatable);
            final Set<Translation> translationList = e.getValue();
            return translationList.stream()
                    .map(translation -> new de.yuga.spacebattle.rest.dto.i18n.Translation(translatable, translation))
                    .collect(Collectors.toList());
        }).flatMap(Collection::stream).collect(Collectors.toList());
        return ResponseEntity.ok(translations);
    }

    @PostMapping(value = "/translations", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the current tick.", operationId = "updateTranslation",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.i18n.Translation.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> updateTranslation(@RequestBody @Nonnull final List<de.yuga.spacebattle.rest.dto.i18n.Translation> translations) {
        PreconditionWebHelper.checkNotNull(translations, "translations must not be empty");

        final Set<Integer> idTranslatables = translations.stream().map(de.yuga.spacebattle.rest.dto.i18n.Translation::getIdTranslatable).collect(Collectors.toSet());
        if (idTranslatables.size() != 1) {
            throw new NotifyWebUserException("There must be only one translatable changed at a time.");
        }
        final Integer idTranslatable = new ArrayList<>(idTranslatables).get(0);
        final Translatable translatable = translatableService.findById(idTranslatable);
        if (translatable == null) {
            throw new NotifyWebUserException("Funny that you try to update a translation which is not present.");
        }
        translations.forEach(translation -> {
            final String translationText = translation.getTranslation();
            final String languageCode = translation.getLanguageCode();
            if (StringUtils.isNotBlank(translationText) && StringUtils.isNotBlank(languageCode))
                translatable.updateOrCreate(languageCode, translationText);
        });
        final Translatable save = translatableService.save(translatable);
        return ResponseEntity.ok(save.getTranslations().stream()
                .map(t -> new de.yuga.spacebattle.rest.dto.i18n.Translation(translatable, t))
                .collect(Collectors.toList()));
    }

    @GetMapping(value = "/buildings")
    @Operation(summary = "Get all building data as csv file.", operationId = "getBuildings",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FileUpload.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getBuildings(final HttpServletResponse response) {

        final String preferredLanguage = getPreferredLanguage();
        final List<Building> all = buildingService.findAll();

        final String content = new BuildingCsvTransformer(false, preferredLanguage).convert(all);
        return ResponseEntity.ok(new FileUpload("building.csv", content));
    }
}
