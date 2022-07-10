package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.UserMessage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface UserMessageRepository extends CrudRepository<UserMessage, Integer> {

    @Nonnull
    @Query(name = "UserMessage.getByIdIfUserIsReceiver")
    Optional<UserMessage> getByIdIfUserIsReceiver(@Param("idUserMessage") final int idUserMessage, @Param("idUser") final int idUser);
}
