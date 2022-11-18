package de.yuga.spacebattle.rest.api.wiki;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.wiki.WikiService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.wiki.Article;
import de.yuga.spacebattle.rest.dto.wiki.ArticleCreate;
import de.yuga.spacebattle.rest.dto.wiki.ArticleEdit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@RestController
@RolesAllowed("USER")
@Tag(name = "WikiApi")
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + WikiApi.ENDPOINT + "/")
public class WikiApi extends BaseApi {

    public final static String ENDPOINT = "wiki";

    @Nonnull
    private final WikiService wikiService;

    public WikiApi(@Nonnull final WikiService wikiService) {
        this.wikiService = Preconditions.checkNotNull(wikiService, "wikiService must not be empty");
    }

    @PostMapping()
    @Operation(summary = "Creates a new article.", operationId = "createArticle",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ArticleCreate.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Article.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> createArticle(@RequestBody @Nonnull final ArticleCreate article) {

        final int idUser = getIdUser();
        final de.yuga.spacebattle.backend.entities.wiki.Article created = wikiService.createArticle(idUser, article);
        return ResponseEntity.ok(new Article(created));
    }

    @PutMapping()
    @Operation(summary = "Creates a new article.", operationId = "editArticle",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ArticleEdit.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Article.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> editArticle(@RequestBody @Nonnull final ArticleEdit article) {

        final int idUser = getIdUser();
        final de.yuga.spacebattle.backend.entities.wiki.Article edited = wikiService.editArticle(idUser, article);
        return ResponseEntity.ok(new Article(edited));
    }
}
