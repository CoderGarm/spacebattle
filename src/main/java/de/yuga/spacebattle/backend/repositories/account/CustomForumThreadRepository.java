package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.dto.forum.IdToId;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomForumThreadRepository {

    @Nonnull
    List<IdToId> findAllIdThreadForForums(List<Integer> idForums);
}
