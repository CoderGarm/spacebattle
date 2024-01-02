package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.MailService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.turn.TickAdviceService;
import de.yuga.spacebattle.rest.dto.turn.TickAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Set;

@Service
public class TickAdviceEMailRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(TickAdviceEMailRunner.class);

    @Nonnull
    private final TickAdviceService tickAdviceService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final MailService mailService;

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private Tick today;

    @Nonnull
    private final String server;

    @Autowired
    public TickAdviceEMailRunner(@Nonnull @Value("${sb.server:localhost}") final String server,
                                 @Nonnull final TickAdviceService tickAdviceService,
                                 @Nonnull final UserService userService,
                                 @Nonnull final MailService mailService) {
        this.server = Preconditions.checkNotNull(server, "server must not be empty");
        this.tickAdviceService = Preconditions.checkNotNull(tickAdviceService, "tickAdviceService must not be empty");
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
        this.mailService = Preconditions.checkNotNull(mailService, "mailService must not be empty");
    }


    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        if (this.server.equals("localhost")) {
            LOGGER.info("Sending nothing - I'm localhost");
            return;
        }

        LOGGER.info("Sending tick advice mails is important every day");

        final Set<User> adviceRecipients = userService.findAdviceRecipients();
        for (final User recipient : adviceRecipients) {
            final TickAdvice tickAdvice = tickAdviceService.getConstructionAdvice(recipient.getId(), Translation.DEFAULT_LANGUAGE);
            mailService.sendAdviceMail(today, recipient, tickAdvice);
        }
    }
}
