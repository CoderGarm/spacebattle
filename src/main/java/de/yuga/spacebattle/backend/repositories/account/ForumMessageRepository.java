package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ForumMessageRepository extends PagingAndSortingRepository<ForumMessage, Integer>, CustomForumMessageRepository {

}
