package de.yuga.spacebattle.rest.config.role;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EGameUserRole;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.config.security.JwtTokenUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


/**
 * This aspect intercepts each method which is annotated with the allowed roles annotation.
 * <p>
 * The method prevents access to the method when the current user has not the permission to access.
 *
 * @author Thomas Hunziker
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@Component
public class EndpointPermissionHandler {

    @Nonnull
    private final JwtTokenUtil tokenUtil;

    @Nonnull
    private final UserService userService;

    @Autowired
    public EndpointPermissionHandler(@Nonnull final JwtTokenUtil tokenUtil, @Nonnull final UserService userService) {
        Preconditions.checkNotNull(tokenUtil, "tokenUtil shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.tokenUtil = tokenUtil;
        this.userService = userService;
    }

    /**
     * Check execution permissions on methods annotated with {@link AllowedRoles}
     *
     * @param joinPoint    join point for which permissions are being checked
     * @param allowedRoles permissions annotates on the method
     */
    @Before("@annotation(allowedRoles)")
    public void checkPermissions(final JoinPoint joinPoint,
                                 @Nullable final AllowedRoles allowedRoles) {
        if (allowedRoles == null || allowedRoles.roles().length == 0) {
            return;
        }
        final Set<EGameUserRole> gameRoles = getGameRolesForRequest();
        final Set<EGameUserRole> allowedRoleSet = Arrays.stream(allowedRoles.roles()).collect(Collectors.toSet());
        final boolean hasAccess = allowedRoleSet.stream().anyMatch(gameRoles::contains);
        if (!hasAccess) {
            throw new HttpForbiddenException("Nope.");
        }
    }

    private Set<EGameUserRole> getGameRolesForRequest() {
        final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new NotifyWebUserException("Nop, you should use at least some kind of a rest patience.");
        }
        final HttpServletRequest request = requestAttributes.getRequest();
        final String token = request.getHeader(AUTHORIZATION);
        final int idUser = tokenUtil.getIdUserFromAccessToken(token);
        final User user = userService.find(idUser);
        if (user == null) {
            throw new HttpForbiddenException("Nope.");
        }
        return user.getGameUserRoles();
    }
}
