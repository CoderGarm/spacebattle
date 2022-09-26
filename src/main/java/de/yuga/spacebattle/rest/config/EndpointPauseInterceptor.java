package de.yuga.spacebattle.rest.config;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * This aspect intercepts each rest endpoint and returns an exception while the tick is running.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 99)
public class EndpointPauseInterceptor {

    @Nonnull
    private final TickService tickService;

    @Autowired
    public EndpointPauseInterceptor(@Nonnull final TickService tickService) {
        Preconditions.checkNotNull(tickService, "tickService shouldn't be null!");

        this.tickService = tickService;
    }


    @Before("@annotation(requestMapping)")
    public void checkPermissions(final JoinPoint joinPoint,
                                 @Nullable final GetMapping requestMapping) {
        checkTicking();
    }

    @Before("@annotation(requestMapping)")
    public void checkPermissions(final JoinPoint joinPoint,
                                 @Nullable final PostMapping requestMapping) {
        checkTicking();
    }

    @Before("@annotation(requestMapping)")
    public void checkPermissions(final JoinPoint joinPoint,
                                 @Nullable final PutMapping requestMapping) {
        checkTicking();
    }

    @Before("@annotation(requestMapping)")
    public void checkPermissions(final JoinPoint joinPoint,
                                 @Nullable final DeleteMapping requestMapping) {
        checkTicking();
    }

    @Before("@annotation(requestMapping)")
    public void checkPermissions(final JoinPoint joinPoint,
                                 @Nullable final PatchMapping requestMapping) {
        checkTicking();
    }

    /**
     * Checks if the server is ticking currently and does not allow any operation while calculating a tick.
     */
    private void checkTicking() {
        if (tickService.isTicking()) {
            throw new NotifyWebUserException("There is a tick running - please wait.");
        }
    }
}
