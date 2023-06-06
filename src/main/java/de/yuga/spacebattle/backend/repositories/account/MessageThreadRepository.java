package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.chat.MessageThread;
import org.springframework.data.repository.CrudRepository;

public interface MessageThreadRepository extends CrudRepository<MessageThread, Integer>, CustomMessageThreadRepository {

}
