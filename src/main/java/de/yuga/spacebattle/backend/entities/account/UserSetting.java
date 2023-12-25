package de.yuga.spacebattle.backend.entities.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.PasswordConverter;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "userSetting", uniqueConstraints = {
        @UniqueConstraint(name = "EMAIL_UK", columnNames = {"email"})
})
@AttributeOverride(name = "id", column = @Column(name = "idUserSetting"))
public class UserSetting extends AbstractEntityKey {

    @Nonnull
    public static final String DEFAULT_PROFILE_PIC = "perspective-dice-six-faces-random";

    @Nonnull
    @NotNull
    @JoinColumn(name = "idUser")
    @OneToOne(optional = false)
    private User user;

    @Nonnull
    @NotNull
    @Column
    @Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,30})", message = "must contain of 8 to 30 characters of numbers, letters, capital letters and special characters")
    @Convert(converter = PasswordConverter.class)
    private String password;

    @Nonnull
    @NotNull
    @Email
    @Size(min = 3, max = 50)
    private String email;

    @Nonnull
    @NotNull
    private final LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Marks if the user must not log in.
     */
    @Column(columnDefinition = "boolean not null default false")
    private final boolean isLoginForbidden = false;

    /**
     * Marks if the user don't want to provide an eMail.
     */
    @Column(columnDefinition = "boolean not null default false")
    private boolean noEMailWanted = false;

    /**
     * Marks if the user has already verified it's eMail.
     */
    @Column(columnDefinition = "boolean not null default false")
    private boolean isEMailVerified = false;

    /**
     * Marks if the user wants to receive eMails about a new release.
     */
    @Column(columnDefinition = "boolean not null default false")
    private boolean receiveChangelogInfos = false;

    /**
     * Marks if the user wants to receive eMails about important empire updates.
     */
    @Column(columnDefinition = "boolean not null default false")
    private boolean receiveTickAdvice = false;

    @Nonnull
    @NotNull
    @Size(min = 1, max = 50)
    @Column(columnDefinition = "varchar(50) default 'perspective-dice-six-faces-random'")
    private String profilePic = DEFAULT_PROFILE_PIC;

    public UserSetting() {
    }

    public UserSetting(@Nonnull final User user,
                       @Nonnull final String email,
                       @Nonnull final String password,
                       final boolean noEMailWanted) {
        this.email = Preconditions.checkNotNull(email, "email must not be empty");
        this.password = Preconditions.checkNotNull(password, "password must not be empty");
        this.user = Preconditions.checkNotNull(user, "user must not be empty");
        this.noEMailWanted = noEMailWanted;
    }

    @Nonnull
    public String getPassword() {
        return password;
    }

    public void setPassword(@Nonnull final String password) {
        Preconditions.checkNotNull(password, "password shouldn't be null!");

        this.password = password;
    }

    @Nonnull
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nonnull String email) {
        this.email = email;
    }

    @Nonnull
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isLoginForbidden() {
        return isLoginForbidden;
    }

    public boolean isNoEMailWanted() {
        return noEMailWanted;
    }

    public void setNoEMailWanted(final boolean noEMailWanted) {
        this.noEMailWanted = noEMailWanted;
    }

    public boolean isEMailVerified() {
        return isEMailVerified;
    }

    public void setEMailVerified(final boolean EMailVerified) {
        isEMailVerified = EMailVerified;
    }

    public boolean isReceiveChangelogInfos() {
        return receiveChangelogInfos;
    }

    public void setReceiveChangelogInfos(final boolean receiveChangelogInfos) {
        this.receiveChangelogInfos = receiveChangelogInfos;
    }

    public boolean isReceiveTickAdvice() {
        return receiveTickAdvice;
    }

    public void setReceiveTickAdvice(final boolean receiveTickAdvice) {
        this.receiveTickAdvice = receiveTickAdvice;
    }

    @Nonnull
    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(@Nonnull final String pic) {
        this.profilePic = Preconditions.checkNotNull(pic, "pic must not be empty");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSetting)) return false;

        UserSetting that = (UserSetting) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }
}
