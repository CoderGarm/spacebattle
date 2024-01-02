package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.repositories.turn.TickRepository;
import de.yuga.spacebattle.backend.services.MailService;
import de.yuga.spacebattle.backend.services.turn.tick.TickRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TickRunnerService {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(TickRunnerService.class);

    @Nonnull
    private final String server;

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private Tick today;

    @Nonnull
    private final TickRepository tickRepository;

    @Nonnull
    private final MailService mailService;

    private boolean isTicking = false;

    @Nonnull
    private final List<TickRunner> tickRunners;


    @Autowired
    public TickRunnerService(@Nonnull @Value("${sb.server:localhost}") final String server,
                             @Nonnull final Set<TickRunner> tickRunners,
                             @Nonnull final TickRepository tickRepository,
                             @Nonnull final MailService mailService) {
        Preconditions.checkNotNull(tickRunners, "tickRunners must not be empty");
        this.tickRunners = tickRunners.stream()
                .filter(TickRunner::isActive)
                .sorted((TickRunner::compareTo))
                .collect(Collectors.toList());

        this.server = Preconditions.checkNotNull(server, "server must not be empty");
        this.tickRepository = Preconditions.checkNotNull(tickRepository, "tickRepository shouldn't be null!");
        this.mailService = Preconditions.checkNotNull(mailService, "mailService must not be empty");
    }

    @PostConstruct
    private void loadTick() {
        this.today = getToday();
    }

    @Scheduled(cron = "${sb.tick.cron}", zone = "Europe/Berlin")
    protected void doIt() {
        doTick();
    }

    public void doTick() {
        final long startB = Calendar.getInstance().getTimeInMillis();

        try {
            loadTick();
            LOGGER.info("Tick scheduled");
            // block all rest endpoints while ticking
            isTicking = true;
            today = tickRepository.save(new Tick());
            LOGGER.info("Today is " + today);

            for (final TickRunner tickRunner : tickRunners) {
                tickRunner.tick(today);
            }

            LOGGER.info("Tick done.");
        } catch (final Exception ex) {
            if (!this.server.equals("localhost")) {
                mailService.sendExceptionMail(ex);
            }
            throw ex;
        } finally {
            today.setTickEnds(LocalDateTime.now());
            tickRepository.save(today);
            LOGGER.info("Tick has processed!");
            final long end = Calendar.getInstance().getTimeInMillis();
            final long duration = (end - startB) / 1000;
            LOGGER.info("{} takes {} seconds", today, duration);
            isTicking = false;
        }
    }

    public boolean isTickPresent() {
        return tickRepository.findAllTicks().isEmpty();
    }

    /**
     * Is non-null after initialization.
     */
    @Nonnull
    @SuppressWarnings("DataFlowIssue")
    public Tick getToday() {
        return tickRepository.getLatest();
    }

    public boolean isTicking() {
        return isTicking;
    }
}
