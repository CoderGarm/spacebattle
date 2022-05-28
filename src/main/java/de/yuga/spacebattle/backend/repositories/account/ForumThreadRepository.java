package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.forum.ForumThread;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ForumThreadRepository extends PagingAndSortingRepository<ForumThread, Integer>, CustomForumThreadRepository {
}
