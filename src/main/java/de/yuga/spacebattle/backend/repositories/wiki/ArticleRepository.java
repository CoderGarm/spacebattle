package de.yuga.spacebattle.backend.repositories.wiki;

import de.yuga.spacebattle.backend.entities.wiki.Article;
import de.yuga.spacebattle.backend.enums.ETutorialCategory;
import de.yuga.spacebattle.backend.enums.EWikiCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface ArticleRepository extends JpaRepository<Article, Integer> {

    @Nullable
    @Query("SELECT a FROM Article a JOIN FETCH a.articleRevisions WHERE a.id = :idArticle")
    Article findByIdWithRevisions(@Param("idArticle") final int idArticle);

    @Nullable
    @Query("SELECT a FROM Article a WHERE a.title LIKE :articleTitle ORDER BY a.title")
    List<Article> findByTitle(@Param("articleTitle") @Nonnull final String articleTitle);

    @Nullable
    @Query("SELECT a FROM Article a WHERE a.wikiCategory = :wikiCategory")
    Set<Article> findArticlesWithType(@Param("wikiCategory") @Nonnull final EWikiCategory wikiCategory);

    @Nullable
    @Query("SELECT a FROM Article a WHERE a.wikiCategory = :wikiCategory AND a.tutorialCategory = :tutorialCategory")
    Set<Article> findArticlesWithTypeAndTutorialCategory(@Param("wikiCategory") @Nonnull final EWikiCategory wikiCategory,
                                                         @Param("tutorialCategory") @Nonnull final ETutorialCategory tutorialCategory);
}
