package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.forum.ForumMessageRead;

public interface CustomForumMessageReadRepository {

    ForumMessageRead create(final int idForum,
                            final int idForumThread,
                            final int idForumMessage,
                            final int idUser);
}
