package de.yuga.spacebattle.backend.entities.wiki;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.wiki.WikiCalculator;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.EDiffDeltaType;
import de.yuga.spacebattle.backend.enums.EWikiCategory;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.wiki.ArticleCreate;
import de.yuga.spacebattle.rest.dto.wiki.ArticleEdit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "article")
@AttributeOverride(name = "id", column = @Column(name = "idArticle"))
public class Article extends AbstractEntityKey {

    /**
     * If the article is a clone of another one, it knows it's parent.
     */
    @Nullable
    @ManyToOne
    @JoinColumn(name = "idBase")
    private Article baseArticle;

    @Nonnull
    @NotNull
    private String title;

    @Nonnull
    @NotNull
    private String langCode;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EWikiCategory wikiCategory;

    @Nonnull
    @NotNull
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private final Set<ArticleRevision> articleRevisions = new HashSet<>();

    public Article() {
    }

    public Article(@Nonnull final User author, @Nonnull final ArticleCreate article) {
        Preconditions.checkNotNull(author, "author must not be empty");
        Preconditions.checkNotNull(article, "article must not be empty");

        this.title = article.getTitle();
        this.langCode = article.getLangCode();
        this.wikiCategory = article.getWikiCategory();
        this.articleRevisions.add(new ArticleRevision(author, 1, this, createArticleLines(article)));
    }

    @Nonnull
    private List<ArticleLine> createArticleLines(@Nonnull final ArticleCreate create) {
        Preconditions.checkNotNull(create, "create must not be empty");

        final String content = create.getContent();
        return WikiCalculator.generateArticleLines(content);
    }

    public void editArticleLines(@Nonnull final User author, @Nonnull final ArticleEdit edit) {
        Preconditions.checkNotNull(author, "author must not be empty");
        Preconditions.checkNotNull(edit, "edit must not be empty");

        final String content = edit.getContent();
        if (StringUtils.isNotEmpty(content)) {
            final List<ArticleLine> articleLines = WikiCalculator.generateArticleLines(content);
            final List<ArticleLine> changes = WikiCalculator.buildDiff(getMergedArticleLines(), articleLines);

            final ArticleRevision latestRev = getArticleRevisions().stream()
                    .sorted()
                    .reduce((o1, o2) -> o2)
                    .orElseThrow(NullPointerException::new);
            int version = latestRev.getVersion() + 1;
            final ArticleRevision revision = new ArticleRevision(author, version, this, changes);
            addArticleRevisions(revision);
        }
    }

    @Nullable
    public Article getBaseArticle() {
        return baseArticle;
    }

    public void setBaseArticle(@Nullable final Article baseArticle) {
        this.baseArticle = baseArticle;
    }

    @Nonnull
    public String getTitle() {
        return title;
    }

    @Nonnull
    public String getLangCode() {
        return langCode;
    }

    @Nonnull
    public EWikiCategory getWikiCategory() {
        return wikiCategory;
    }

    @Nonnull
    public Set<ArticleRevision> getArticleRevisions() {
        if (!hasRevisionsInitialized()) {
            throw new NotifyWebUserException("Please avoid that.");
        }
        return articleRevisions;
    }

    public boolean hasRevisionsInitialized() {
        return Persistence.getPersistenceUtil().isLoaded(this, "articleRevisions");
    }

    public void setTitle(@Nonnull final String title) {
        Preconditions.checkNotNull(title, "title must not be empty");

        this.title = title;
    }

    public void setWikiCategory(@Nonnull final EWikiCategory wikiCategory) {
        Preconditions.checkNotNull(wikiCategory, "wikiCategory must not be empty");

        this.wikiCategory = wikiCategory;
    }

    public void addArticleRevisions(@Nonnull final ArticleRevision articleRevision) {
        Preconditions.checkNotNull(articleRevision, "articleRevision must not be empty");

        this.articleRevisions.add(articleRevision);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Article article = (Article) o;

        return new EqualsBuilder().append(id, article.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    @Nonnull
    public List<ArticleLine> getMergedArticleLines() {
        if (articleRevisions.isEmpty()) {
            return List.of();
        }
        final List<ArticleRevision> forArticle = articleRevisions.stream()
                .sorted()
                .collect(Collectors.toList());
        final ArticleRevision articleRevision = forArticle.get(0);
        if (forArticle.size() == 1) {
            return new ArrayList<>(articleRevision.getArticleLines());
        }
        final List<ArticleLine> lines = new ArrayList<>();
        forArticle.forEach(rev -> {
            final List<ArticleLine> articleLines = rev.getArticleLines();
            articleLines.forEach(articleLine -> {
                final EDiffDeltaType deltaType = articleLine.getDeltaType();
                final int lineNo = articleLine.getLineNo();
                switch (deltaType) {
                    case DELETE:
                        //noinspection SimplifyOptionalCallChains
                        final ArticleLine existing = lines.stream().filter(l -> l.getLineNo() == articleLine.getLineNo() && l.getContent().equals(articleLine.getContent()))
                                .findFirst()
                                .orElse(null);
                        if (existing != null) {
                            lines.remove(existing);
                        }
                        break;
                    case INSERT:
                        lines.add(lineNo, new ArticleLine(lineNo, EDiffDeltaType.INSERT, articleLine.getContent()));
                        break;
                    default:
                        throw new UnsupportedOperationException("Why does this happen?");
                }
            });
        });
        return lines;
    }
}
