package de.yuga.spacebattle.backend.services.wiki;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.wiki.Article;
import de.yuga.spacebattle.backend.entities.wiki.ArticleLine;
import de.yuga.spacebattle.backend.entities.wiki.ArticleRevision;
import de.yuga.spacebattle.backend.enums.EWikiCategory;
import de.yuga.spacebattle.backend.repositories.wiki.ArticleRepository;
import de.yuga.spacebattle.backend.repositories.wiki.ArticleRevisionRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.dto.wiki.ArticleCreate;
import de.yuga.spacebattle.rest.dto.wiki.ArticleEdit;
import de.yuga.spacebattle.rest.dto.wiki.ArticlePlainContent;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WikiService {

    @Nonnull
    private final ArticleRepository articleRepository;

    @Nonnull
    private final ArticleRevisionRepository revisionRepository;

    @Nonnull
    private final UserService userService;

    public WikiService(@Nonnull final ArticleRepository articleRepository,
                       @Nonnull final ArticleRevisionRepository revisionRepository,
                       @Nonnull final UserService userService) {
        this.articleRepository = Preconditions.checkNotNull(articleRepository, "articleRepository must not be empty");
        this.revisionRepository = Preconditions.checkNotNull(revisionRepository, "revisionRepository must not be empty");
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
    }

    @Nonnull
    public List<Article> findAll() {
        return Objects.requireNonNullElse(articleRepository.findAll(), new ArrayList<>());
    }

    @Nullable
    public ArticlePlainContent findLatestContentForArticle(final int idArticle) {
        final Article article = Preconditions.checkNotNull(articleRepository.findByIdWithRevisions(idArticle), "article must not be empty");
        final Set<ArticleRevision> revisions = article.getArticleRevisions();

        if (revisions.isEmpty()) {
            return null;
        }
        final List<ArticleRevision> forArticle = revisions.stream()
                .sorted()
                .collect(Collectors.toList());
        final List<ArticleLine> lines = article.getMergedArticleLines();
        final ArticleRevision latest = forArticle.get(forArticle.size() - 1);
        final ArticleRevision result = new ArticleRevision(latest.getAuthor(), latest.getVersion(), latest.getArticle(), lines);
        return new ArticlePlainContent(result);
    }

    @Nonnull
    public List<ArticleRevision> getArticleRevisions(final int idArticle) {
        return Objects.requireNonNullElse(revisionRepository.findForArticle(idArticle), new ArrayList<>());
    }

    public Article createArticle(final int idUser, @Nonnull final ArticleCreate create) {
        Preconditions.checkNotNull(create, "create must not be empty");

        final User author = Preconditions.checkNotNull(userService.find(idUser), "author must not be empty");
        final Article a = new Article(author, create);
        return articleRepository.save(a);
    }

    public Article editArticle(final int idUser, @Nonnull final ArticleEdit edit) {
        Preconditions.checkNotNull(edit, "edit must not be empty");

        final User author = Preconditions.checkNotNull(userService.find(idUser), "author must not be empty");
        final Article article = Preconditions.checkNotNull(articleRepository.findByIdWithRevisions(edit.getIdArticle()), "article must not be empty");
        article.editArticleLines(author, edit);
        return articleRepository.save(article);
    }

    public List<Article> findByTitle(@Nonnull final String articleTitle) {
        Preconditions.checkNotNull(articleTitle, "articleTitle must not be empty");

        return Objects.requireNonNullElse(articleRepository.findByTitle(articleTitle + "%"), new ArrayList<>());
    }

    /**
     * The home article is the welcome message on the home screen.
     */
    @Nullable
    public ArticlePlainContent findHomeArticle(@Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        final Set<Article> basedOn = Objects.requireNonNullElse(articleRepository.findArticlesWithType(EWikiCategory.WELCOME_MESSAGE), new HashSet<>());
        Article languageBasedArticle = filterByLangCode(basedOn, preferredLanguage);
        if (languageBasedArticle == null) {
            languageBasedArticle = filterByLangCode(basedOn, Translation.DEFAULT_LANGUAGE);
        }
        if (languageBasedArticle != null) {
            return findLatestContentForArticle(languageBasedArticle.getId());
        }
        return null;
    }

    @Nullable
    private Article filterByLangCode(@Nonnull final Set<Article> basedOn, @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(basedOn, "basedOn must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        return basedOn.stream()
                .filter(a -> a.getLangCode().equals(preferredLanguage))
                .findFirst()
                .orElse(null);
    }
}
