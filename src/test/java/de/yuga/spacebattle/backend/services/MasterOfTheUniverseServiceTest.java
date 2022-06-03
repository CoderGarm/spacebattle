package de.yuga.spacebattle.backend.services;

import de.yuga.spacebattle.SpringBootTestProfile;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import de.yuga.spacebattle.backend.entities.account.forum.ForumThread;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("not needed for unit or integration testing")
@SpringBootTestProfile
public class MasterOfTheUniverseServiceTest {

    @Autowired
    private MasterOfTheUniverseService masterOfTheUniverseService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    @Test
    public void createInitialData() {
        masterOfTheUniverseService.createInitialData();
    }

    @Test
    void testCreateForums() {
        masterOfTheUniverseService.createForums();
    }

    @Test
    void createSomeForums() {

        final List<User> users = userService.findAll();

        final List<Forum> forums = forumService.findAll();
        forums.forEach(forum -> {
            final User user = users.stream().filter(forum::isUserAllowed).findFirst().orElse(null);
            assertNotNull(user);

            final ForumThread forumThread = new ForumThread(forum, "Thread in " + forum.getTitle(), "Description in " + forum.getDescription());
            final ForumThread save = forumService.save(forumThread);
            final ForumMessage forumMessage = new ForumMessage(save, user, "Hello World!");
            forumService.save(forumMessage);
        });
    }

    @Test
    void createThread() {
        final Forum forum = forumService.findForumById(1);
        assertNotNull(forum);
        final ForumThread forumThread = new ForumThread(forum, "Thread in " + forum.getTitle(), "Description in " + forum.getDescription());
        forumService.save(forumThread);
    }
}
