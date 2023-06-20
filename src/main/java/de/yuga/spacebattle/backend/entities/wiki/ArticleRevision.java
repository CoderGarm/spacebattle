package de.yuga.spacebattle.backend.entities.wiki;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.wiki.WikiCalculator;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "articleRevision")
@AttributeOverride(name = "id", column = @Column(name = "idArticleRevision"))
public class ArticleRevision extends AbstractEntityKey implements Comparator<ArticleRevision>, Comparable<ArticleRevision> {

    @Nonnull
    @NotNull
    @OneToOne
    @JoinColumn(name = "idAuthor")
    private User author;

    @NotNull
    private int version;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idArticle")
    private Article article;

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "articleLines", joinColumns = @JoinColumn(name = "idArticleRevision"))
    private List<ArticleLine> articleLines = new ArrayList<>();

    public ArticleRevision() {
    }

    public ArticleRevision(@Nonnull final User author,
                           final int version,
                           @Nonnull final Article article,
                           @Nonnull final List<ArticleLine> articleLines) {
        Preconditions.checkNotNull(author, "author must not be empty");
        Preconditions.checkNotNull(article, "article must not be empty");
        Preconditions.checkNotNull(articleLines, "articleLines must not be empty");

        this.author = author;
        this.version = version;
        this.article = article;
        this.articleLines = articleLines;
    }

    @Nonnull
    public User getAuthor() {
        return author;
    }

    public int getVersion() {
        return version;
    }

    @Nonnull
    public Article getArticle() {
        return article;
    }

    @Nonnull
    public List<ArticleLine> getArticleLines() {
        return articleLines.stream().sorted().collect(Collectors.toList());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final ArticleRevision that = (ArticleRevision) o;

        return new EqualsBuilder().append(version, that.version).append(article, that.article).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(version).append(article).toHashCode();
    }

    @Override
    public int compare(final ArticleRevision o1, final ArticleRevision o2) {
        return Integer.compare(o1.getVersion(), o2.getVersion());
    }

    public String asPlainString() {
        return WikiCalculator.getAsPlainString(getArticleLines());
    }

    @Override
    public int compareTo(@Nonnull final ArticleRevision o) {
        return compare(this, o);
    }
}
