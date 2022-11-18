package de.yuga.spacebattle.backend.repositories.wiki;

import de.yuga.spacebattle.backend.entities.wiki.ArticleRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;

public interface ArticleRevisionRepository extends JpaRepository<ArticleRevision, Integer> {

    @Nullable
    @Query("SELECT rev FROM ArticleRevision rev WHERE rev.article.id = :idArticle ORDER BY rev.version ASC")
    List<ArticleRevision> findForArticle(@Param("idArticle") final int idArticle);
}
