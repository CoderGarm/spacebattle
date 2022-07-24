package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.SpringBootTestProfile;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Please run with empty message read table. Adapt accordingly when tests where interesting.")
@SpringBootTestProfile
class ForumMessageReadRepositoryTest {

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    @Test
    void testIsMessageUnread() {
        final boolean result = forumService.isMessageUnread(1, 1, 1);
        assertFalse(result);
    }

    @Test
    void testHasThreadUnread() {
        final boolean result = forumService.hasThreadUnread(1, 1);
        assertFalse(result);
    }

    @Test
    void testHasForumUnread() {
        final boolean result = forumService.hasForumUnread(1, 1);
        assertFalse(result);
    }

    @Test
    void testHasUserUnread() {
        final User user = userService.find(1);
        assertNotNull(user);
        final boolean result = forumService.hasUserUnread(user);
        assertFalse(result);
    }
}
