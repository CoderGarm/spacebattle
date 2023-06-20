package de.yuga.spacebattle.backend.entities.combined.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EApplicationState;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "allianceApplication")
@AttributeOverride(name = "id", column = @Column(name = "idAllianceApplication"))
public class AllianceApplication extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idAlliance")
    private Alliance alliance;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idUser")
    private User applicant;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTickAppliedAt")
    private Tick appliedAt;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idTickDecidedAt")
    private Tick decidedAt;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EApplicationState applicationState = EApplicationState.OPEN;

    public AllianceApplication() {
    }

    public AllianceApplication(@Nonnull final Alliance alliance, @Nonnull final User applicant, @Nonnull final Tick applied) {
        this.alliance = Preconditions.checkNotNull(alliance, "alliance must not be empty");
        this.applicant = Preconditions.checkNotNull(applicant, "applicant must not be empty");
        this.appliedAt = Preconditions.checkNotNull(applied, "applied must not be empty");
    }

    @Nonnull
    public Alliance getAlliance() {
        return alliance;
    }

    @Nonnull
    public User getApplicant() {
        return applicant;
    }

    @Nonnull
    public Tick getAppliedAt() {
        return appliedAt;
    }

    @Nullable
    public Tick getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(@Nonnull final Tick decidedAt) {
        Preconditions.checkNotNull(decidedAt, "decidedAt must not be empty");

        if (this.decidedAt == null) {
            this.decidedAt = decidedAt;
        }
    }

    @Nonnull
    public EApplicationState getApplicationState() {
        return applicationState;
    }

    public void setApplicationState(@Nonnull final EApplicationState applicationState) {
        Preconditions.checkNotNull(applicationState, "applicationState must not be empty");

        this.applicationState = applicationState;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final AllianceApplication that = (AllianceApplication) o;

        return new EqualsBuilder().append(alliance, that.alliance).append(applicant, that.applicant).append(appliedAt, that.appliedAt).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(alliance).append(applicant).append(appliedAt).toHashCode();
    }
}
