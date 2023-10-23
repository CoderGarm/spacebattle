package de.yuga.spacebattle.rest.api.wiki;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.enums.ETutorialCategory;
import de.yuga.spacebattle.backend.services.wiki.WikiService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.wiki.Article;
import de.yuga.spacebattle.rest.dto.wiki.ArticlePlainContent;
import de.yuga.spacebattle.rest.dto.wiki.ArticleRevision;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PUBLIC_BASE_ENDPOINT;

@RestController
@Tag(name = "WikiApi")
@RequestMapping(value = "/" + PUBLIC_BASE_ENDPOINT + "/" + WikiApi.ENDPOINT + "/")
public class PublicWikiApi extends BaseApi {

    @Nonnull
    private final WikiService wikiService;

    public PublicWikiApi(@Nonnull final WikiService wikiService) {
        this.wikiService = Preconditions.checkNotNull(wikiService, "wikiService must not be empty");
    }

    @GetMapping()
    @Operation(summary = "Get all articles.", operationId = "getAllArticles",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Article.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllArticles() {
        final List<de.yuga.spacebattle.backend.entities.wiki.Article> all = wikiService.findAll();
        return ResponseEntity.ok(all.stream().map(Article::new).collect(Collectors.toList()));
    }

    @GetMapping(value = "/home")
    @Operation(summary = "Returns the latest content of the requested article.", operationId = "getHomeArticle",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ArticlePlainContent.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getHomeArticle() {

        final String preferredLanguage = getPreferredLanguage();
        final ArticlePlainContent content = wikiService.findHomeArticle(preferredLanguage);
        if (content == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(content);
    }

    @GetMapping(value = "/{idArticle}")
    @Operation(summary = "Returns the latest content of the requested article.", operationId = "getArticleLatestContent",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ArticlePlainContent.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getArticleLatestContent(@PathVariable("idArticle") final int idArticle) {
        final ArticlePlainContent content = wikiService.findLatestContentForArticle(idArticle);
        PreconditionWebHelper.checkNotNull(content, "content must not be empty");

        return ResponseEntity.ok(content);
    }

    @GetMapping(value = "tutorial/{tutorialCategory}")
    @Operation(summary = "Returns the latest content of the requested article.", operationId = "getTutorialArticle",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ArticlePlainContent.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getTutorialArticle(@PathVariable("tutorialCategory") final ETutorialCategory tutorialCategory) {
        final ArticlePlainContent content = wikiService.getTutorialArticle(tutorialCategory, getPreferredLanguage());
        return ResponseEntity.ok(content);
    }

    @GetMapping(value = "/autocomplete/{articleTitle}")
    @Operation(summary = "Returns all article which are matching a left-side search for the title.", operationId = "getArticleForAutocomplete",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Article.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getArticleForAutocomplete(@PathVariable("articleTitle") final String articleTitle) {
        if (StringUtils.isBlank(articleTitle)) {
            return ResponseEntity.ok(List.of());
        }

        final List<de.yuga.spacebattle.backend.entities.wiki.Article> content = wikiService.findByTitle(articleTitle);
        return ResponseEntity.ok(content.stream().map(Article::new).collect(Collectors.toList()));
    }

    @GetMapping(value = "/revisions/{idArticle}")
    @Operation(summary = "Returns all revisions of the requested article.", operationId = "getArticleRevisions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getArticleRevisions(@PathVariable("idArticle") final int idArticle) {
        final List<de.yuga.spacebattle.backend.entities.wiki.ArticleRevision> revisions = wikiService.getArticleRevisions(idArticle);
        return ResponseEntity.ok(revisions.stream().map(ArticleRevision::new).collect(Collectors.toList()));
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

}
