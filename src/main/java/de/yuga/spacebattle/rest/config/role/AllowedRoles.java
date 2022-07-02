package de.yuga.spacebattle.rest.config.role;

import de.yuga.spacebattle.backend.enums.EGameUserRole;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Restricts rest endpoints by defined roles.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@AuthenticationPrincipal
public @interface AllowedRoles {

    /**
     * The roles which should be allowed.
     */
    EGameUserRole[] roles() default {};
}
