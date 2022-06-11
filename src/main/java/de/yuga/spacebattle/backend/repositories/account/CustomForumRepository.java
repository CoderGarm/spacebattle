package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;

import javax.annotation.Nonnull;

public interface CustomForumRepository {

    @Nonnull
    Forum getAllianceForumForUser(Alliance alliance);
}
