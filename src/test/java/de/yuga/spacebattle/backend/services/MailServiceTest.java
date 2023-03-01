package de.yuga.spacebattle.backend.services;

import de.yuga.spacebattle.SpringBootProdProfile;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootProdProfile
@Disabled("not needed for unit or integration testing")
class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Test
    void sendMail() {
        final User flash = userService.find(1);
        assertNotNull(flash);
        mailService.sendMailVerificationMessage(flash);
    }
}
