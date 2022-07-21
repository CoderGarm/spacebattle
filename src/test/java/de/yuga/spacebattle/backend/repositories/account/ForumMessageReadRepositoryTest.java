package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.SpringBootTestProfile;
import de.yuga.spacebattle.backend.services.account.ForumService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Please run with empty message read table. Adapt accordingly when tests where interesting.")
@SpringBootTestProfile
class ForumMessageReadRepositoryTest {

    @Autowired
    private ForumService forumService;

    @Test
    void testIsMessageUnread() {
        final boolean result = forumService.isMessageUnread(1, 1, 1);
        assertTrue(result);
    }

    @Test
    void testHasThreadUnread() {
        final boolean result = forumService.hasThreadUnread(1, 1);
        assertTrue(result);
    }

    @Test
    void testHasForumUnread() {
        final boolean result = forumService.hasForumUnread(1, 1);
        assertTrue(result);
    }

    @Test
    void testHasUserUnread() {
        final boolean result = forumService.hasUserUnread(1);
        assertTrue(result);
    }
}
