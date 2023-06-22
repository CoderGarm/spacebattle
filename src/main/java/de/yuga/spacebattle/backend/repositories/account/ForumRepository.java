package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ForumRepository extends PagingAndSortingRepository<Forum, Integer>, JpaRepository<Forum, Integer>, CustomForumRepository {

}
