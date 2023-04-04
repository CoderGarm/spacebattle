package de.yuga.spacebattle.backend;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.MailService;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Nonnull;

//@Aspect
//@Component
//@Order(Ordered.LOWEST_PRECEDENCE - 101)
public class ExceptionInterceptor {

    private final boolean mailExceptions;

    @Nonnull
    private final MailService mailService;

    //@Autowired
    public ExceptionInterceptor(@Nonnull @Value("${logging.mail.exceptions:false}") final String mailExceptions,
                                @Nonnull final MailService mailService) {
        this.mailExceptions = Boolean.parseBoolean(Preconditions.checkNotNull(mailExceptions, "mailExceptions must not be empty"));
        this.mailService = Preconditions.checkNotNull(mailService, "mailService must not be empty");
    }

    //@AfterThrowing(pointcut = "execution(* de.yuga.spacebattle..* (..))", throwing = "ex") /* todo tomcat didn't start */
    public void handleError(Exception ex) {
        if (mailExceptions) {
            mailService.sendExceptionMail(ex);
        }
    }
}
