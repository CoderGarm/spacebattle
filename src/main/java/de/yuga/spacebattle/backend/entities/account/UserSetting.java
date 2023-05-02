package de.yuga.spacebattle.backend.entities.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "userSetting")
@AttributeOverride(name = "id", column = @Column(name = "idUserSetting"))
public class UserSetting extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idUser")
    private User user;

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

    public UserSetting() {
    }

    public UserSetting(@Nonnull final User user, final boolean noEMailWanted) {
        this.user = Preconditions.checkNotNull(user, "user must not be empty");
        this.noEMailWanted = noEMailWanted;
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
