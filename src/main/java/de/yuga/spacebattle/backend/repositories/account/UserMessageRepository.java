package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.chat.UserMessage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface UserMessageRepository extends CrudRepository<UserMessage, Integer> {

    @Nonnull
    @Query(name = "UserMessage.getByIdIfUserIsReceiver")
    Optional<UserMessage> getByIdIfUserIsReceiver(@Param("idUserMessage") final int idUserMessage, @Param("idUser") final int idUser);

    @Query("SELECT CASE WHEN (COUNT(msg) > 0) THEN TRUE ELSE FALSE END FROM UserMessage msg WHERE msg.messageThread.id = :idMessageThread AND msg.sender.id <> :idReceiver AND msg.receivedAt IS NULL")
    boolean hasUnreadMessages(@Param("idReceiver") final int idReceiver, @Param("idMessageThread") final int idMessageThread);

    @Query("SELECT CASE WHEN (COUNT(msg) > 0) THEN TRUE ELSE FALSE END " +
            "FROM MessageThread t " +
            "LEFT JOIN UserMessage msg ON (t = msg.messageThread)" +
            "WHERE msg.sender.id <> :idReceiver " +
            "AND (t.userOne.id = :idReceiver OR t.userTwo.id = :idReceiver) " +
            "AND msg.receivedAt IS NULL")
    boolean hasUserUnreadMessages(@Param("idReceiver") final int idReceiver);
}
