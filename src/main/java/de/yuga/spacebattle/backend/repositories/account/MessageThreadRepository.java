package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.chat.MessageThread;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public interface MessageThreadRepository extends CrudRepository<MessageThread, Integer>, CustomMessageThreadRepository {

    @Nullable
    @Query("SELECT m FROM MessageThread m WHERE m.userOne = :user OR m.userTwo = :user")
    Set<MessageThread> findAllForAuthor(@Nonnull final User user);
}
