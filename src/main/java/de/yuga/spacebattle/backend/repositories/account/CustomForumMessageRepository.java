package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.dto.forum.IdToId;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomForumMessageRepository {

    @Nonnull
    List<IdToId> findAllMessageIdsForThreadId(List<Integer> idForumThreads);

    @Nonnull
    List<ForumMessage> findReportsWithUserWithPaging(int idForumThread, int page, int size);
}
