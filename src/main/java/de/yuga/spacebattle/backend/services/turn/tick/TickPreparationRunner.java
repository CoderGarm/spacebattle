package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.caches.DisabledWhileTicking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Set;

@Service
public class TickPreparationRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(TickPreparationRunner.class);

    @Nonnull
    private final Set<DisabledWhileTicking> disabledWhileTickings;

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private Tick today;


    public TickPreparationRunner(@Nonnull final Set<DisabledWhileTicking> disabledWhileTickings) {
        this.disabledWhileTickings = Preconditions.checkNotNull(disabledWhileTickings, "disabledWhileTickings must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("TickPreparation is important every day");
        LOGGER.info("Disabling caches");
        disabledWhileTickings.forEach(DisabledWhileTicking::disable);
    }

}
