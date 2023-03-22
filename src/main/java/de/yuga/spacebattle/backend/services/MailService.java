package de.yuga.spacebattle.backend.services;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    @Nonnull
    private final JavaMailSender emailSender;

    @Autowired
    public MailService(@Nonnull final JavaMailSender emailSender) {
        this.emailSender = Preconditions.checkNotNull(emailSender, "emailSender must not be empty");
    }

    public void sendMailVerificationMessage(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        if (user.isNoEMailWanted()) {
            LOGGER.info("eMail verification will not processed for '" + user.getUsername() + "'");
            return;
        }

        sendMail(user.getEmail(), templateMailVerification(user));
    }


    public void sendMailChangePasswordMessage(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        if (user.isNoEMailWanted() || !user.isEMailVerified() || user.isLoginForbidden()) {
            LOGGER.info("Password change eMail will not processed for '" + user.getUsername() + "'");
            return;
        }

        sendMail(user.getEmail(), templatePasswordChange(user));
    }

    @Nonnull
    private SimpleMailMessage templatePasswordChange(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@battleforhonor.de");
        message.setSubject("Password change requested");
        message.setText("You can change your password by clicking the link or copying it into your browser's address line.\n\n\n" + getPasswordChangeURL(user));
        return message;
    }

    @Nonnull
    private String getPasswordChangeURL(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        return "https://www.battleforhonor.de/forgotten-password/" + getUserIdentificationCode(user);
    }

    private void sendMail(@Nonnull final String destination, @Nonnull final SimpleMailMessage message) {
        Preconditions.checkNotNull(destination, "destination must not be empty");

        message.setTo(destination);
        try {
            emailSender.send(message);
            LOGGER.info("Mail sent to '" + destination + "'");
        } catch (final MailException ex) {
            LOGGER.warn("Mail sending failed to '" + destination + "'", ex);
        }
    }

    @Nonnull
    private SimpleMailMessage templateMailVerification(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@battleforhonor.de");
        message.setSubject("eMail Verification");
        message.setText("Please verify your email by clicking the link or copying it into your browser's address line.\n\n\n" + getVerificationURL(user));
        return message;
    }

    @Nonnull
    private String getVerificationURL(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        return "https://www.battleforhonor.de/api/public/auth/verify/" + getUserIdentificationCode(user);
    }

    @Nonnull
    private String getUserIdentificationCode(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        final String hash = user.getPassword();
        final int id = user.getId();
        return hash + "-" + id;
    }

    @Nonnull
    public static VerificationParameter getParametersFromVerificationCode(@Nonnull final String code) {
        Preconditions.checkNotNull(code, "code must not be empty");

        final String[] split = code.split("-");
        return new VerificationParameter(split[1], split[0]);
    }

    public void sendExceptionMail(@Nonnull final Exception ex) {
        Preconditions.checkNotNull(ex, "ex must not be empty");

        if (ex instanceof NotifyWebUserException && !((NotifyWebUserException) ex).isLoggingNecessary()) {
            return;
        }
        final SimpleMailMessage mailException = templateMailException(ex);
        emailSender.send(mailException);
    }

    @Nonnull
    private SimpleMailMessage templateMailException(@Nonnull final Exception ex) {
        Preconditions.checkNotNull(ex, "ex must not be empty");

        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@battleforhonor.de");
        message.setTo("webmaster@battleforhonor.de");
        message.setSubject("Exception occurred: " + ex.getMessage());

        if (ex instanceof NotifyWebUserException && ((NotifyWebUserException) ex).isLoggingNecessary()) {
            message.setText(ex.toString());
        }
        message.setText(ExceptionUtils.getStackTrace(ex));

        return message;
    }

    public static class VerificationParameter {

        @Nonnull
        private final String id;

        @Nonnull
        private final String password;

        public VerificationParameter(@Nonnull final String id, @Nonnull final String password) {
            this.id = Preconditions.checkNotNull(id, "id must not be empty");
            this.password = Preconditions.checkNotNull(password, "password must not be empty");
        }

        public int getId() {
            return Integer.parseInt(id);
        }

        public boolean verifyUser(@Nonnull final User user) {
            Preconditions.checkNotNull(user, "user must not be empty");

            return password.equals(user.getPassword());
        }
    }
}
